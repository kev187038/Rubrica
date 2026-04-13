import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import javax.swing.*;

public class LoginFrame extends JFrame {

    private final Vector<User> utenti;
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    public LoginFrame(Vector<User> utenti) {
        super("Accesso — Rubrica");
        this.utenti = utenti;

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        form.add(labeled("Username:", usernameField));
        form.add(labeled("Password:", passwordField));

        JButton accedi = new JButton("Accedi");
        JButton registrati = new JButton("Registrati");

        accedi.addActionListener(e -> accedi());
        registrati.addActionListener(e -> registrati());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(registrati);
        buttons.add(accedi);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private static JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.add(new JLabel(label), BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private User trovaUtente(String username) {
        for (int i = 0; i < utenti.size(); i++) {
            User u = utenti.get(i);
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    private void accedi() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserire username e password.", "Errore", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User u = trovaUtente(username);
        if (u == null || !u.passwordMatches(password)) {
            JOptionPane.showMessageDialog(this, "Username o password non validi.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dispose();
        //apri finestra rubrixca per utente loggato
        main.openRubricaWindow(u);
    }

    //registrare nuovo utente e ritorno a login
    private void registrati() {
        JTextField uField = new JTextField(16);
        JPasswordField p1 = new JPasswordField(16);
        JPasswordField p2 = new JPasswordField(16);
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(labeled("Username:", uField));
        p.add(labeled("Password:", p1));
        p.add(labeled("Conferma password:", p2));

        int res = JOptionPane.showConfirmDialog(this, p, "Nuovo utente", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        String username = uField.getText().trim();
        String pass1 = new String(p1.getPassword());
        String pass2 = new String(p2.getPassword());

        if (!main.isValidUsernameForStorage(username)) {
            JOptionPane.showMessageDialog(this,
                    "Username non valido. Usare solo lettere, numeri, underscore, trattino o punto.",
                    "Errore",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (pass1.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La password non può essere vuota.", "Errore", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!pass1.equals(pass2)) {
            JOptionPane.showMessageDialog(this, "Le password non coincidono.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (trovaUtente(username) != null) {
            JOptionPane.showMessageDialog(this, "Username non disponibile.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        utenti.add(new User(username, pass1));
        try {
            UserFileStore.saveAll(utenti);
        } catch (IOException ex) {
            utenti.removeElementAt(utenti.size() - 1);
            JOptionPane.showMessageDialog(this, "Impossibile salvare l'utente: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File informazioniDir = PersonaFileStore.getInformazioniDirForUser(username);
        if (!informazioniDir.exists() && !informazioniDir.mkdirs()) {
            JOptionPane.showMessageDialog(this, "Impossibile creare la cartella dati per l'utente.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Registrazione completata. Puoi accedere.", "OK", JOptionPane.INFORMATION_MESSAGE);
    }
}
