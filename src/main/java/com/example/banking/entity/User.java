@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String fullName;
    private String address;
    private String username;
    private String password;
    private double balance = 0.0;
    // getters/setters
}