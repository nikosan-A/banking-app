@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder encoder;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(username -> {
            User user = userRepo.findByUsername(username);
            if (user == null) throw new UsernameNotFoundException(username);
            return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), List.of(new SimpleGrantedAuthority("USER")));
        }).passwordEncoder(encoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/register", "/login").permitAll()
            .anyRequest().authenticated()
            .and().formLogin().loginPage("/login").defaultSuccessUrl("/dashboard", true)
            .and().logout().logoutSuccessUrl("/login?logout");
    }
}