package P1;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import org.apache.tika.sax.Link;
import java.util.Map.Entry;

import org.apache.tika.metadata.Metadata;

public class OutputHelper {
  // Exporta un HashMap a CSV
  public static void csvWriter(List<Entry<String, Integer>> entries, String pathname) throws IOException {
    String eol = System.getProperty("line.separator");

    try (Writer writer = new FileWriter(pathname + ".csv")) {
      writer.append("Text;Size").append(eol);
      for (Entry<String, Integer> entry : entries) {
        writer.append(entry.getKey()).append(';').append(Integer.toString(entry.getValue())).append(eol);
      }
    } catch (IOException ex) {
      ex.printStackTrace(System.err);
    }
  }

  // Exporta a tabla con titulo, tipo, codificacion y lenguaje.
  public static void csvWriterMetadata(List<String> file_names, List<String> languages,
      List<Metadata> metadatos, String pathname) throws IOException {
    String eol = System.getProperty("line.separator");
    int contador = 0;
    try (Writer writer = new FileWriter(pathname + ".csv")) {
      writer.append("Name;Type;Encoding;Language").append(eol);
      for (Metadata metadata_object : metadatos) {
        writer.append(file_names.get(contador)).append(";").append(metadata_object.get(Metadata.CONTENT_TYPE))
            .append(";").append(metadata_object.get(Metadata.CONTENT_ENCODING)).append(";")
            .append((languages.get(contador))).append(eol);
        contador += 1;
      }
    } catch (IOException ex) {
      ex.printStackTrace(System.err);
    }
  }

  public static void csvWriterLinks(List<Link> links, String pathname) {
    String eol = System.getProperty("line.separator");
    try(Writer writer = new FileWriter(pathname+".csv")) {
      writer.append("Links").append(eol);
      for (Link link : links) {
        writer.append(link.getUri()).append(eol);
      }
    }catch (IOException ex) {
      ex.printStackTrace(System.err);
    }
  }
}
