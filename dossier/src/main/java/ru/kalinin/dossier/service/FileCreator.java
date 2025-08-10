package ru.kalinin.dossier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.kalinin.dossier.dto.CreditDto;
import ru.kalinin.dossier.dto.PaymentScheduleElementDto;
import ru.kalinin.dossier.exception.FileCreatorException;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileCreator {

    public Path createTxtFile(CreditDto creditDto) {
        log.info("Create temporary file with txt format");

        Path tempFile;
        try {
            tempFile = Files.createTempFile("loan_documents", ".txt");

                try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(tempFile, StandardOpenOption.WRITE))) {

                    writer.write("Сумма кредита " + creditDto.getAmount() + "\n");
                    writer.write("Срок " + creditDto.getTerm() + "\n");
                    writer.write("Ежемесячный платеж " + creditDto.getMonthlyPayment() + "\n");
                    writer.write("Ставка " + creditDto.getRate() + "\n");
                    writer.write("ПСК " + creditDto.getPsk() + "\n");

                    if (Boolean.TRUE.equals(creditDto.getIsInsuranceEnabled())) {
                        writer.write("Страхование подключено\n");
                    } else {
                        writer.write("Страхование не подключено\n");
                    }

                    if (Boolean.TRUE.equals(creditDto.getIsSalaryClient())) {
                        writer.write("Зарплатный клиент - Да\n\n");
                    } else {
                        writer.write("Зарплатный клиент - Нет\n\n");
                    }

                    writer.write(String.format("%-6s | %-10s | %-15s | %-20s | %-25s | %-15s%n",
                            "Номер", "Дата", "Общий платеж", "Платеж по процентам", "Платеж по основному долгу", "Остаток долга"));
                    if (creditDto.getPaymentSchedule() != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        for (PaymentScheduleElementDto payment : creditDto.getPaymentSchedule()) {
                            writer.write(String.format("%-6d | %-10s | %-15.2f | %-20.2f | %-25.2f | %-15.2f%n",
                                    payment.getNumber(),
                                    payment.getDate().format(formatter),
                                    payment.getTotalPayment(),
                                    payment.getInterestPayment(),
                                    payment.getDebtPayment(),
                                    payment.getRemainingDebt()));
                        }
                    }
                    log.info("Файл успешно создан.");
            }
        } catch (IOException exception) {
            log.error("File creating exception: " + exception.getMessage());
            throw new FileCreatorException(exception.getMessage());
        }

        return tempFile;
    }
}
