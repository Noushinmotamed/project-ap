
import com.httpHandler.*;
import com.models.Restaurant;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

/**
 * HttpHandler classes : check token,get request, check required fields, check formats, make object if necessary
 * Controller classes : check fields of created object and other logics
 * DAO classes : anything that requires working with db
 */
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
        server.createContext("/restaurant", new RestaurantHttpHandler());
        server.createContext("/admin",new AdminHttpHandler());
        server.createContext("/deliveries", new DeliveryHttpHandler());
        server.createContext("/transactions", new WalletHttpHandler());
        server.start();
        System.out.println("Server started on port 8080...");
    }
}
