@Entity
public class Transaction {
    @Id @GeneratedValue
    private Long id;
    private Long userId;
    private String type;
    private double amount;
    private LocalDateTime timestamp;
    // getters/setters
}