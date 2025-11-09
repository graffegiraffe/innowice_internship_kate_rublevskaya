package by.rublevskaya.userservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "card_info", schema = "userservice")
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = "user")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CardInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @NotBlank(message = "Card number is mandatory")
    @Size(min = 16, max = 16, message = "Card number must be 16 digits")
    @Column(name = "number", nullable = false, length = 16)
    private String number;

    @Column(name = "holder", length = 100)
    private String holder;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;
}