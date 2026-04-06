@Controller
public class BankingController {
    @Autowired private UserRepository userRepo;
    @Autowired private TransactionRepository txRepo;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = userRepo.findByUsername(principal.getName());
        model.addAttribute("user", user);
        return "dashboard";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam double amount, Principal principal) {
        User user = userRepo.findByUsername(principal.getName());
        user.setBalance(user.getBalance() + amount);
        userRepo.save(user);

        Transaction tx = new Transaction();
        tx.setUserId(user.getId());
        tx.setType("DEPOSIT");
        tx.setAmount(amount);
        tx.setTimestamp(LocalDateTime.now());
        txRepo.save(tx);

        return "redirect:/receipt/" + tx.getId();
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam double amount, Principal principal) {
        User user = userRepo.findByUsername(principal.getName());
        if(user.getBalance() >= amount) {
            user.setBalance(user.getBalance() - amount);
            userRepo.save(user);

            Transaction tx = new Transaction();
            tx.setUserId(user.getId());
            tx.setType("WITHDRAW");
            tx.setAmount(amount);
            tx.setTimestamp(LocalDateTime.now());
            txRepo.save(tx);

            return "redirect:/receipt/" + tx.getId();
        }
        return "redirect:/dashboard?error=InsufficientFunds";
    }

    @GetMapping("/receipt/{id}")
    public String receipt(@PathVariable Long id, Model model) {
        Transaction tx = txRepo.findById(id).orElseThrow();
        model.addAttribute("transaction", tx);
        return "receipt";
    }
}