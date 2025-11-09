package by.rublevskaya.userservice.exception;

public class CardNumberExistsException extends RuntimeException {
    public CardNumberExistsException(String number) {
        super("Card with number '" + number + "' already exists for this user");
    }
}