package ru.hh.school;

/**
 * Hello world!
 *
 */
public class App 
{
    private class AppSubClass {
        private final int parameter;

        AppSubClass(int parameter) {
            this.parameter = parameter;
        }

        /**
         * @return the subClassParameter
         */
        public int getParameter() {
            return parameter;
        }
    }
    
    private final AppSubClass subClassInstance;

    App(int parameter) {
        subClassInstance = new AppSubClass(parameter);
    }

    public int getParameter() {
        return subClassInstance.getParameter();
    }

    public static void main( String[] args )
    {
        App app = new App(10);
        System.out.println( "Hello World!" );
        System.out.println("subClassParameter = " + app.getParameter());
    }
}
