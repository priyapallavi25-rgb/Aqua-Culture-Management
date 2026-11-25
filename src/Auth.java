import java.sql.*;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;
import java.security.MessageDigest;

public class Auth {

    private static final String URL =
            "jdbc:mysql://localhost:3306/Aquaculture";

    private static final String USER = "root";

    private static final String PASSWORD = 
            System.getenv("MYSQL_PASSWORD");


    // Connect to MySQL
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }


    // Generate password hash
    private static String hashPassword(String password) throws Exception {

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        int iterations = 210000;

        KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                256
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] hash = factory.generateSecret(spec).getEncoded();

        return iterations + ":"
                + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(hash);
    }


    // Verify password
    private static boolean verifyPassword(
            String password, String storedHash) throws Exception {

        String[] parts = storedHash.split(":");

        int iterations = Integer.parseInt(parts[0]);

        byte[] salt = Base64.getDecoder().decode(parts[1]);

        byte[] originalHash = Base64.getDecoder().decode(parts[2]);

        KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                256
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] enteredHash = factory.generateSecret(spec).getEncoded();

        return MessageDigest.isEqual(originalHash, enteredHash);
    }


    // Register user
    public static boolean register(
            String username,
            String email,
            String password) {

        String query =
                "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            String hashedPassword = hashPassword(password);

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);

            stmt.executeUpdate();

            return true;

        } catch (SQLIntegrityConstraintViolationException e) {

            System.out.println("Username or email already exists.");
            return false;

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }


    // Login user
    public static boolean login(String email, String password) {

        String query =
                "SELECT password_hash FROM users WHERE email = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                String storedHash = rs.getString("password_hash");

                return verifyPassword(password, storedHash);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }
}