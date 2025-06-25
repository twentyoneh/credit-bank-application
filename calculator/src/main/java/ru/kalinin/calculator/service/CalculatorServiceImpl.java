package ru.kalinin.calculator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kalinin.common.dto.*;
import ru.kalinin.common.enums.EmploymentStatus;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculatorServiceImpl implements CalculatorService {

    // Базовая процентная ставка
    BigDecimal baseRate = new BigDecimal("15.0");

    /**
     * Генерирует список возможных кредитных предложений на основе данных заявки.
     *
     * @param request параметры заявки на кредит
     * @return список вариантов кредитных предложений
     *
     * @apiNote Возвращает 4 варианта предложений с разными условиями (страховка/зарплатный клиент).
     */
    @Override
    public List<LoanOfferDto> calculateOffers(LoanStatementRequestDto request) {
//        preScoring(request);

        List<LoanOfferDto> offers = new ArrayList<>();

        offers.add(addOrder(request,false,false));
        offers.add(addOrder(request,true,true));
        offers.add(addOrder(request,true,false));
        offers.add(addOrder(request,false,true));

        offers.sort(Comparator.comparing(LoanOfferDto::getRate).reversed());
        return offers;
    }

    /**
     * Рассчитывает параметры кредита на основе детальных данных скоринга.
     *
     * @param data Данные для скоринга и расчёта кредита
     * @return Кредит с рассчитанными параметрами, графиком платежей и ПСК
     *
     * @apiNote Выполняет скоринг, рассчитывает ставку, ежемесячный платёж, ПСК и график платежей.
     */
    @Override
    public CreditDto calculateCredit(ScoringDataDto data) {
        BigDecimal rate = scoring(data);
        BigDecimal amount = data.getAmount();
        int term = data.getTerm();

        BigDecimal monthlyPayment = calculateMonthlyPayment(amount, rate, term);
        List<PaymentScheduleElementDto> schedule = generatePaymentSchedule(amount, rate, term);
        BigDecimal psk = calculatePSK(monthlyPayment,term,amount);

        CreditDto creditDto = new CreditDto();
        creditDto.setAmount(amount);
        creditDto.setTerm(term);
        creditDto.setMonthlyPayment(monthlyPayment);
        creditDto.setRate(rate);
        creditDto.setPsk(psk);
        creditDto.setIsInsuranceEnabled(data.getIsInsuranceEnabled());
        creditDto.setIsSalaryClient(data.getIsSalaryClient());
        creditDto.setPaymentSchedule(schedule);

        return creditDto;
    }

    /**
     * Генерирует график платежей по кредиту.
     *
     * @param amount Сумма кредита
     * @param rate Годовая процентная ставка (в процентах)
     * @param term Срок кредита в месяцах
     * @return Список элементов графика платежей (номер, дата, сумма, проценты, основной долг, остаток)
     *
     * @apiNote Формирует аннуитетный график платежей с разбивкой на проценты и основной долг.
     * @operationId generatePaymentSchedule
     * @response 200 График платежей успешно сформирован
     */
    private List<PaymentScheduleElementDto> generatePaymentSchedule(BigDecimal amount, BigDecimal rate, int term) {
        List<PaymentScheduleElementDto> schedule = new ArrayList<>();
        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP); // месячная процентная ставка
        BigDecimal monthlyPayment = calculateMonthlyPayment(amount, rate, term); // аннуитетный ежемесячный платёж
        BigDecimal remainingDebt = amount; // остаток долга

        for (int month = 1; month <= term; month++) {
            BigDecimal interestPayment = remainingDebt.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP); // расчёт процентной части платежа за текущий месяц
            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment); // расчёт основной части платежа (погашение долга)
            if (month == term) {
                monthlyPayment = principalPayment.add(interestPayment);
            }
            PaymentScheduleElementDto element = new PaymentScheduleElementDto();
            element.setNumber(month);
            element.setDate(LocalDate.now().plusMonths(month - 1));
            element.setTotalPayment(monthlyPayment);
            element.setInterestPayment(interestPayment);
            element.setDebtPayment(principalPayment);
            element.setRemainingDebt(remainingDebt.subtract(principalPayment));
            schedule.add(element);

            remainingDebt = remainingDebt.subtract(principalPayment);
        }
        return schedule;
    }

    /**
     * Рассчитывает полную стоимость кредита (ПСК) в процентах.
     *
     * @param monthlyPayment ежемесячный платёж
     * @param term срок кредита в месяцах
     * @param amount сумма кредита
     * @return ПСК (полная стоимость кредита) в процентах
     *
     * @apiNote ПСК = ((сумма всех выплат - сумма кредита) / сумма кредита) * 100
     */
    private BigDecimal calculatePSK(BigDecimal monthlyPayment, int term, BigDecimal amount) {
        // Сумма всех выплат за весь срок кредита
        BigDecimal totalPayments = monthlyPayment.multiply(BigDecimal.valueOf(term));
        // ПСК = ((сумма выплат - сумма кредита) / сумма кредита) * 100
        BigDecimal psk = totalPayments.subtract(amount)
                .divide(amount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return psk.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Выполняет скоринг заявки и рассчитывает итоговую процентную ставку по кредиту.
     * <p>
     * Учитывает возраст, зарплату, статус занятости, стаж, должность, семейное положение и пол заёмщика.
     * В случае несоответствия критериям выбрасывает IllegalArgumentException с причиной отказа.
     *
     * @param data Данные для скоринга (ScoringDataDto)
     * @return Итоговая процентная ставка (BigDecimal)
     * @throws IllegalArgumentException если заявка не проходит скоринг
     */
    private BigDecimal scoring(ScoringDataDto data) {
        BigDecimal rate = baseRate;
        int age = Period.between(data.getBirthdate(), LocalDate.now()).getYears();

        // Проверка возраста
        if(age < 20 || age > 65){
            throw new IllegalArgumentException("Отказ: возраст вне допустимого диапазона");
        }

        // Проверка максимальной суммы кредита (не более 24 зарплат)
        BigDecimal maxLoan = data.getEmployment().getSalary().multiply(BigDecimal.valueOf(24));
        if(data.getAmount().compareTo(maxLoan) > 0) {
            throw new IllegalArgumentException("Отказ: сумма кредита превышает 24 зарплаты");
        }

        EmploymentDto emp = data.getEmployment();
        // Проверка статуса занятости
        if(emp.getEmploymentStatus() == EmploymentStatus.UNEMPLOYED){
            throw new IllegalArgumentException("Отказ: безработный");
        }

        // Проверка стажа работы (общий и на текущем месте)
        Integer monthsOfExecution = emp.getWorkExperienceTotal();
        Integer monthsCurrentJob = emp.getWorkExperienceCurrent();
        if(monthsOfExecution < 18 || monthsCurrentJob < 3){
            throw new IllegalArgumentException("Отказ: недостаточный стаж");
        }

        // Корректировка ставки по статусу занятости
        switch (emp.getEmploymentStatus()) {
            case SELF_EMPLOYED -> rate = rate.add(BigDecimal.valueOf(2));
            case BUSINESS_OWNER -> rate = rate.add(BigDecimal.valueOf(1));
        }

        // Корректировка ставки по должности
        switch(emp.getPosition()) {
            case MID_MANAGER -> rate = rate.subtract(BigDecimal.valueOf(2));
            case TOP_MANAGER -> rate = rate.subtract(BigDecimal.valueOf(3));
        }

        // Корректировка ставки по семейному положению
        switch (data.getMaritalStatus()){
            case MARRIED -> rate = rate.subtract(BigDecimal.valueOf(3));
            case DIVORCED -> rate = rate.add(BigDecimal.valueOf(1));
        }

        // Корректировка ставки по полу и возрасту
        switch (data.getGender()){
            case NON_BINARY -> rate = rate.add(BigDecimal.valueOf(7));
            case FEMALE -> {
                if (age >= 32 && age <= 60)
                    rate = rate.subtract(BigDecimal.valueOf(3));
            }
            case MALE -> {
                if(age >= 30 && age <= 55)
                    rate = rate.subtract(BigDecimal.valueOf(2));
            }
        }

        return rate;
    }

    /**
     * Формирует кредитное предложение на основе параметров заявки и выбранных опций.
     * <p>
     * Учитывает наличие страховки и статус зарплатного клиента для корректировки ставки и суммы.
     *
     * @param requestDto         параметры заявки на кредит
     * @param isInsuranceEnabled выбрана ли страховка
     * @param isSalaryClient     является ли клиент зарплатным
     * @return LoanOfferDto      сформированное кредитное предложение
     */
    private LoanOfferDto addOrder(LoanStatementRequestDto requestDto,
                                  Boolean isInsuranceEnabled, Boolean isSalaryClient) {

        // Стоимость страховки
        BigDecimal insuranceCost = new BigDecimal("100000");
        BigDecimal amount = requestDto.getAmount();
        Integer term = requestDto.getTerm();

        // Базовая ставка и сумма
        BigDecimal rate = baseRate;
        BigDecimal totalAmount = amount;

        // Если выбрана страховка, уменьшаем ставку и увеличиваем сумму кредита на стоимость страховки
        if (isInsuranceEnabled) {
            rate = rate.subtract(new BigDecimal("3.0"));
            totalAmount = totalAmount.add(insuranceCost);
        }
        // Если клиент зарплатный, уменьшаем ставку
        if (isSalaryClient) {
            rate = rate.subtract(new BigDecimal("1.0"));
        }

        // Расчёт ежемесячного платежа по аннуитетной формуле
        BigDecimal monthlyPayment = calculateMonthlyPayment(totalAmount, rate, term);

        // Формирование DTO предложения
        LoanOfferDto offer = new LoanOfferDto();
        offer.setRequestedAmount(amount);
        offer.setTotalAmount(totalAmount);
        offer.setTerm(term);
        offer.setRate(rate);
        offer.setMonthlyPayment(monthlyPayment);
        offer.setIsInsuranceEnabled(isInsuranceEnabled);
        offer.setIsSalaryClient(isSalaryClient);

        return offer;
    }

    /**
         * Вычисляет ежемесячный аннуитетный платёж по кредиту.
         *
         * @param amount сумма кредита
         * @param rate годовая процентная ставка (в процентах)
         * @param term срок кредита в месяцах
         * @return ежемесячный платёж по кредиту
         *
         * @apiNote Использует аннуитетную формулу для расчёта платежа.
         */
        private BigDecimal calculateMonthlyPayment(BigDecimal amount, BigDecimal rate, Integer term) {
            BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);
            BigDecimal numerator = monthlyRate.multiply(amount);
            BigDecimal denominator = BigDecimal.ONE.subtract(
                    BigDecimal.ONE.add(monthlyRate).pow(-term, new MathContext(10, RoundingMode.HALF_UP))
            );
            return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        }
}
