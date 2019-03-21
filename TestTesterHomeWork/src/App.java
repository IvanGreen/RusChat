import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class App {

    public static void main(String[] args) {

        int array[] = {1, 5, 3, 4, 2, 4, 3, 4, 3, 5, 3, 7};
        Method method = new Method();
        System.out.println(Arrays.toString(method.methodOne(array)));
        System.out.println(method.methodTwo(method.methodOne(array)));
    }
}
