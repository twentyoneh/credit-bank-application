// ...existing code...
// Было:
public CalculatorControllerImpl(CalculatorServiceImpl calculatorService) {
    this.calculatorService = calculatorService;
}
// Стало:
public CalculatorControllerImpl(CalculatorService calculatorService) {
    this.calculatorService = calculatorService;
}
// ...existing code...
