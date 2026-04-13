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

//come PersonaFileStore ma per utenti
public final class UserFileStore {

    private static final String DIR_NAME = "informazioni-utenti";
    private static final String CHARSET = "UTF-8";
    private static final Pattern FILE_PATTERN = Pattern.compile("User(\\d+)\\.txt", Pattern.CASE_INSENSITIVE);

    private UserFileStore() {
    }

    public static File getUtentiDir() {
        return new File(DIR_NAME);
    }

    //carica utenti per login
    public static void loadInto(Vector<User> utenti) throws IOException {
        File dir = getUtentiDir();
        if (!dir.isDirectory()) {
            utenti.clear();
            return;
        }

        File[] files = dir.listFiles((d, name) -> FILE_PATTERN.matcher(name).matches());
        if (files == null) {
            throw new IOException("Impossibile leggere la cartella: " + dir);
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

        Vector<User> loaded = new Vector<>();
        for (FileWithIndex e : entries) {
            loaded.add(readUser(e.file));
        }
        utenti.clear();
        utenti.addAll(loaded);
    }

    public static void saveAll(Vector<User> utenti) throws IOException {
        File dir = getUtentiDir();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Impossibile creare la cartella: " + dir);
        }

        File[] old = dir.listFiles((d, name) -> FILE_PATTERN.matcher(name).matches());
        if (old != null) {
            for (File f : old) {
                if (!f.delete()) {
                    throw new IOException("Impossibile eliminare: " + f);
                }
            }
        }

        for (int i = 0; i < utenti.size(); i++) {
            File file = new File(dir, "User" + (i + 1) + ".txt");
            writeUser(file, utenti.get(i));
        }
    }

    private static User readUser(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file);
                Scanner scanner = new Scanner(in, CHARSET)) {
            String username = readLine(scanner);
            String password = readLine(scanner);
            return new User(username, password);
        }
    }

    private static String readLine(Scanner scanner) throws IOException {
        if (!scanner.hasNextLine()) {
            throw new IOException("File utente incompleto o corrotto");
        }
        return scanner.nextLine();
    }

    private static void writeUser(File file, User u) throws IOException {
        try (PrintStream out = new PrintStream(new FileOutputStream(file), false, CHARSET)) {
            out.println(u.getUsername());
            out.println(u.getPassword());
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
