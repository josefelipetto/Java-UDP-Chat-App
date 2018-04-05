package Server;

public interface iUsuario {
    boolean compare(String typedPassword);

    String hash(String password);
}
