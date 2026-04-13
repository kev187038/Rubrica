import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.*;

//classe editor-persona
public class EditorPersona extends JDialog {

    private final JTextField nomeField = new JTextField(20);
    private final JTextField cognomeField = new JTextField(20);
    private final JTextField indirizzoField = new JTextField(20);
    private final JTextField telefonoField = new JTextField(20);
    private final JTextField etaField = new JTextField(20);

    private final Persona target;
    private final Runnable onSaved;

    public EditorPersona(JFrame owner, Persona target, Runnable onSaved) {
        super(owner, "Editor-persona", true);
        this.target = target;
        this.onSaved = onSaved;

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        form.add(row("Nome:", nomeField));
        form.add(row("Cognome:", cognomeField));
        form.add(row("Indirizzo:", indirizzoField));
        form.add(row("Telefono:", telefonoField));
        form.add(row("Età:", etaField));

        if (target != null) {
            nomeField.setText(target.getNome());
            cognomeField.setText(target.getCognome());
            indirizzoField.setText(target.getIndirizzo());
            telefonoField.setText(target.getTelefono());
            etaField.setText(String.valueOf(target.getEta()));
        }

        JButton salva = new JButton("Salva");
        JButton annulla = new JButton("Annulla");

        salva.addActionListener(e -> salva());
        annulla.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(salva);
        buttons.add(annulla);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private static JPanel row(String labelText, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.add(new JLabel(labelText), BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    //salvare persona
    private void salva() {
        String nome = nomeField.getText().trim();
        String cognome = cognomeField.getText().trim();
        String indirizzo = indirizzoField.getText().trim();
        String telefono = telefonoField.getText().trim();
        String etaText = etaField.getText().trim();

        if (nome.isEmpty() || cognome.isEmpty() || indirizzo.isEmpty() || telefono.isEmpty() || etaText.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Compilare tutti i campi.",
                    "Errore",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int eta;
        //controllo che l'età sia un numero intero valido
        try {
            eta = Integer.parseInt(etaText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "L'età deve essere un numero intero valido.",
                    "Errore",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        //se target è null, aggiungi la persona, altrimenti modifica la persona
        if (target == null) {
            main.aggiungiPersona(new Persona(nome, cognome, indirizzo, telefono, eta));
        } else {
            target.setNome(nome);
            target.setCognome(cognome);
            target.setIndirizzo(indirizzo);
            target.setTelefono(telefono);
            target.setEta(eta);
        }

        onSaved.run();
        dispose();
    }
}
