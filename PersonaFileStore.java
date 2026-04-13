import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PersonaFileStore {

    private static final String INFORMAZIONI_SUBDIR = "informazioni";
    private static final String CHARSET = "UTF-8";
    private static final Pattern FILE_PATTERN = Pattern.compile("Persona(\\d+)\\.txt", Pattern.CASE_INSENSITIVE);

    private PersonaFileStore() {
    }

    /** Cartella {@code dati/<usernameSicuro>/informazioni}. */
    public static File getInformazioniDirForUser(String usernameSanitized) {
        return new File(new File("dati", usernameSanitized), INFORMAZIONI_SUBDIR);
    }

    public static void loadInto(Vector<Persona> rubrica, File informazioniDir) throws IOException {
        if (!informazioniDir.isDirectory()) {
            rubrica.clear();
            return;
        }

        File[] files = informazioniDir.listFiles((d, name) -> FILE_PATTERN.matcher(name).matches());
        if (files == null) {
            throw new IOException("Impossibile leggere la cartella: " + informazioniDir);
        }

        List<FileWithIndex> entries = new ArrayList<>();
        for (File file : files) {
            Matcher m = FILE_PATTERN.matcher(file.getName());
            if (m.matches()) {
                int index = Integer.parseInt(m.group(1));
                entries.add(new FileWithIndex(file, index));
            }
        }

        Collections.sort(entries, Comparator.comparingInt(a -> a.index));

        Vector<Persona> loaded = new Vector<>();
        for (FileWithIndex e : entries) {
            loaded.add(readPersona(e.file));
        }
        rubrica.clear();
        rubrica.addAll(loaded);
    }

    public static void saveAll(Vector<Persona> rubrica, File informazioniDir) throws IOException {
        if (!informazioniDir.exists() && !informazioniDir.mkdirs()) {
            throw new IOException("Impossibile creare la cartella: " + informazioniDir);
        }

        File[] old = informazioniDir.listFiles((d, name) -> FILE_PATTERN.matcher(name).matches());
        if (old != null) {
            for (File f : old) {
                if (!f.delete()) {
                    throw new IOException("Impossibile eliminare: " + f);
                }
            }
        }

        for (int i = 0; i < rubrica.size(); i++) {
            File file = new File(informazioniDir, "Persona" + (i + 1) + ".txt");
            writePersona(file, rubrica.get(i));
        }
    }

    private static Persona readPersona(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file);
                Scanner scanner = new Scanner(in, CHARSET)) {
            String nome = readLine(scanner);
            String cognome = readLine(scanner);
            String indirizzo = readLine(scanner);
            String telefono = readLine(scanner);
            String etaLine = readLine(scanner);
            int eta = Integer.parseInt(etaLine.trim());
            return new Persona(nome, cognome, indirizzo, telefono, eta);
        } catch (NumberFormatException e) {
            throw new IOException("Età non valida in " + file.getPath(), e);
        }
    }

    private static String readLine(Scanner scanner) throws IOException {
        if (!scanner.hasNextLine()) {
            throw new IOException("File incompleto o corrotto");
        }
        return scanner.nextLine();
    }

    private static void writePersona(File file, Persona p) throws IOException {
        try (PrintStream out = new PrintStream(new FileOutputStream(file), false, CHARSET)) {
            out.println(p.getNome());
            out.println(p.getCognome());
            out.println(p.getIndirizzo());
            out.println(p.getTelefono());
            out.println(p.getEta());
        }
    }

    private static final class FileWithIndex {
        final File file;
        final int index;

        FileWithIndex(File file, int index) {
            this.file = file;
            this.index = index;
        }
    }
}
