import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.util.Optional;
import java.util.Vector;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class main {
    public static final Vector<User> utenti = new Vector<User>();
    public static final Vector<Persona> Rubrica = new Vector<Persona>();

    private static final Pattern USERNAME_SAFE = Pattern.compile("^[a-zA-Z0-9_.-]+$");

    public static boolean isValidUsernameForStorage(String username) {
        return username != null && !username.isEmpty() && USERNAME_SAFE.matcher(username).matches();
    }

    public static void aggiungiPersona(Persona persona) {
        Rubrica.add(persona);
    }

    public static void rimuoviPersona(Persona persona) {
        Rubrica.remove(persona);
    }

    public static Persona ricercaPersona(String telefono) {
        for (Persona persona : Rubrica) {
            if (persona.getTelefono().equals(telefono)) {
                return persona;
            }
        }
        return null;
    }

    public static void modificaPersona(
            Persona p,
            Optional<String> nome,
            Optional<String> cognome,
            Optional<String> indirizzo,
            Optional<String> telefono,
            Optional<Integer> eta) {

        if (nome.isPresent()) {
            p.setNome(nome.get());
        }
        if (cognome.isPresent()) {
            p.setCognome(cognome.get());
        }
        if (indirizzo.isPresent()) {
            p.setIndirizzo(indirizzo.get());
        }
        if (telefono.isPresent()) {
            p.setTelefono(telefono.get());
        }
        if (eta.isPresent()) {
            p.setEta(eta.get());
        }
    }

    private static void persistRubrica(Component parent, java.io.File informazioniDir) {
        try {
            PersonaFileStore.saveAll(Rubrica, informazioniDir);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Impossibile salvare i dati: " + e.getMessage(),
                    "Errore",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static DefaultTableModel createRubricaTableModel() {
        String[] cols = {"Nome", "Cognome", "Telefono"};
        return new DefaultTableModel(cols, 0) {
            @Override //togliere edit per i campi
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static void fillTableFromRubrica(DefaultTableModel model) {
        model.setRowCount(0);
        for (Persona p : Rubrica) {
            model.addRow(new Object[]{p.getNome(), p.getCognome(), p.getTelefono()});
        }
    }

    public static void openRubricaWindow(User loggedUser) {
        final java.io.File informazioniDir = PersonaFileStore.getInformazioniDirForUser(loggedUser.getUsername());

        Rubrica.clear();
        try {
            PersonaFileStore.loadInto(Rubrica, informazioniDir);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Impossibile caricare la rubrica: " + e.getMessage(),
                    "Errore",
                    JOptionPane.ERROR_MESSAGE);
        }

        JFrame frame = new JFrame("Rubrica di " + loggedUser.getUsername());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLayout(new BorderLayout());

        DefaultTableModel tableModel = createRubricaTableModel();
        fillTableFromRubrica(tableModel);
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFocusable(true);

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        JButton nuovoButton = new JButton("Nuovo");
        JButton removeButton = new JButton("Elimina");
        JButton modificaButton = new JButton("Modifica");
        JButton cambiaUtenteButton = new JButton("Cambia utente");

        nuovoButton.setToolTipText("Aggiungi un nuovo contatto");
        modificaButton.setToolTipText("Modifica il contatto selezionato");
        removeButton.setToolTipText("Elimina il contatto selezionato");
        cambiaUtenteButton.setToolTipText("Torna al login e cambia utente");

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(nuovoButton);
        toolBar.add(modificaButton);
        toolBar.add(removeButton);
        toolBar.addSeparator();
        toolBar.add(cambiaUtenteButton);
        frame.add(toolBar, BorderLayout.NORTH);

        Runnable refreshTable = () -> fillTableFromRubrica(tableModel);
        Runnable refreshAndPersist = () -> {
            refreshTable.run();
            persistRubrica(frame, informazioniDir);
        };

        nuovoButton.addActionListener(e -> {
            EditorPersona editor = new EditorPersona(frame, null, refreshAndPersist);
            editor.setVisible(true);
        });

        modificaButton.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Per modificare è necessario prima selezionare una persona.",
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            Persona selected = Rubrica.get(modelRow);
            EditorPersona editor = new EditorPersona(frame, selected, refreshAndPersist);
            editor.setVisible(true);
        });

        removeButton.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Per rimuovere è necessario prima selezionare una persona.",
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            Persona selected = Rubrica.get(modelRow);
            int result = JOptionPane.showConfirmDialog(
                    frame,
                    "Eliminare la persona " + selected.getNome() + " " + selected.getCognome() + "?",
                    "Conferma",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                rimuoviPersona(selected);
                refreshAndPersist.run();
            }
        });

        cambiaUtenteButton.addActionListener(e -> {
            frame.dispose();
            Rubrica.clear();
            SwingUtilities.invokeLater(() -> new LoginFrame(utenti).setVisible(true));
        });

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UserFileStore.loadInto(utenti);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Impossibile caricare gli utenti: " + e.getMessage(),
                    "Errore",
                    JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> new LoginFrame(utenti).setVisible(true));
    }
}
