
import com.httpHandler.*;
import com.models.Restaurant;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server =HttpServer.create(new InetSocketAddress(8080), 0);
        //set endpoints here
        server.createContext("/auth/register", new UserHttpHandler());
        server.createContext("/auth/login", new UserHttpHandler());
        server.createContext("/auth/logout", new UserHttpHandler());
        server.createContext("/auth/profile", new ProfileHttpHandler());
        server.createContext("/wallet/top-up", new WalletHttpHandler());
        server.createContext("/payment/online", new PaymentHttpHandler());
        server.createContext("/restaurant/item", new FoodItemHttpHandler());
        server.start();
        System.out.println("Server started on port 8080...");
    }
}
