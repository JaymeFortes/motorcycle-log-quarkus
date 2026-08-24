package org.acme.models;

import java.time.Instant;

import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @UserDefinition registra esta entidade no quarkus-security-jpa: em tempo de
 *                 build, a extensao gera um JpaIdentityProvider que sabe buscar
 *                 um User pelo
 *                 campo @Username e comparar a senha informada com o hash
 *                 em @Password.
 *                 Isso e o que AuthService usa (via IdentityProviderManager)
 *                 para validar o
 *                 login sem reimplementar a comparacao de bcrypt na mao.
 */
@Entity
@Table(name = "users")
@UserDefinition
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Username nao precisa se chamar "username": aqui o login e feito pelo e-mail.
    @Username
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String name;

    // @Password espera, por padrao, um hash BCrypt (Modular Crypt Format) -
    // exatamente o formato que BcryptUtil.bcryptHash() produz em AuthService.
    @Password
    @Column(nullable = false)
    private String password_hash;

    // @Roles aceita uma ou mais roles separadas por virgula; aqui usamos uma.
    @Roles
    @Column(nullable = false)
    private String role = "user";

    @Column(nullable = false)
    private Instant created_at = Instant.now();

    public User() {
    }

    public User(String email, String name, String password_hash) {
        this.email = email;
        this.name = name;
        this.password_hash = password_hash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }
}
