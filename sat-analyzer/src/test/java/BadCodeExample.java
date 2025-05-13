public class BadCodeExample {

    public void example1() {
        // Дублирование кода
        String result = "hello" + " world" + "!";
        System.out.println(result);
        String anotherResult = "hello" + " world" + "!";
        System.out.println(anotherResult);
    }

    public void example2() {
        // Сложный метод, который делает слишком много
        int a = 10;
        int b = 20;
        int c = 30;
        int d = 40;
        int e = 50;

        // Все вычисления в одном месте
        int result = a + b;
        result = result * c;
        result = result / d;
        result = result - e;
        System.out.println(result);
    }

    public void example3() {
        // Неиспользуемые переменные
        int unusedVar1 = 100;
        String unusedVar2 = "test";
        boolean unusedVar3 = true;

        int a = 5;
        int b = 10;
        System.out.println(a + b);  // Без использования unusedVar1, unusedVar2, unusedVar3
    }

    public void example4() {
        // Проблемы с производительностью
        int sum = 0;
        for (int i = 0; i < 1000000; i++) {
            for (int j = 0; j < 1000000; j++) {
                sum += i * j;
            }
        }
        System.out.println(sum);  // Неоптимизированный вложенный цикл
    }

    public void example5() {
        // Плохое именование переменных
        int x = 100;  // Что это за переменная? Почему x?
        int y = 200;  // Что это за переменная? Почему y?
        System.out.println(x + y);
    }

    public void example6() {
        // Проблемы с контролем за исключениями
        try {
            int result = 10 / 0;  // Деление на ноль
        } catch (Exception e) {
            System.out.println("Произошла ошибка");
        }

        // И никаких действий после ошибки. Логирование или переработка исключения были бы более полезными.
    }

    public static void main(String[] args) {
        BadCodeExample example = new BadCodeExample();
        example.example1();
        example.example2();
        example.example3();
        example.example4();
        example.example5();
        example.example6();
    }
}