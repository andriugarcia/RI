import java.io.File;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.language.LanguageIdentifier;

public class EjemploSimple {
  public static void main(String[] args) throws Exception {

    // Creaamos una instancia de Tika con la configuracion por defecto
    Tika tika = new Tika();
    // Se parsean todos los ficheros pasados como argumento y se extrae el contenido
    for (String file : args) {
      File f = new File(file);

      String type = tika.detect(f);
      System.out.println(file + ":" + type);

      String text = tika.parseToString(f);
      System.out.print(text);

      LanguageIdentifier identifier = new LanguageIdentifier("This a text with an unknown idiom");

      System.out.println("Escrito en :" + identifier.getLanguage());
    }
  }
}