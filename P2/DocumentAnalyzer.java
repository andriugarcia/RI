package P2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Paths;
import java.text.ParseException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.store.Directory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.Link;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;

import org.apache.lucene.analysis.core.*;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.hunspell.Dictionary;
import org.apache.lucene.analysis.hunspell.HunspellStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.UAX29URLEmailTokenizer;
import org.apache.lucene.store.FSDirectory;

public class DocumentAnalyzer {
  private String nombre;
  private String contenido;
  private Metadata metadata; // cambio el atributo de tipo a Metadata, para facilitar el acceso a los
                             // valores.
  private List<Link> enlaces;
  private LanguageResult language;

  String getNombre() {
    return nombre;
  }

  LanguageResult getLanguageResult() {
    return language;
  }

  List<Link> getEnlaces() {
    return enlaces;
  }

  Metadata getMetadata() {
    return metadata;
  }

  String getContenido() {
    return contenido;
  }

  DocumentAnalyzer(File file) throws Exception {
    this.nombre = file.getName();
    FileInputStream inputStream = new FileInputStream(file); // creamos el inputstream
    BodyContentHandler contentHandler = new BodyContentHandler(-1);
    this.metadata = new Metadata();
    ParseContext parser = new ParseContext();
    LinkContentHandler linkContentHandler = new LinkContentHandler();
    TeeContentHandler teeContentHandler = new TeeContentHandler(linkContentHandler, contentHandler);
    AutoDetectParser autodetectParser = new AutoDetectParser();

    autodetectParser.parse(inputStream, teeContentHandler, metadata, parser);
    LanguageDetector identifier = new OptimaizeLangDetector().loadModels();
    this.contenido = contentHandler.toString();
    this.language = identifier.detect(this.contenido);
    this.enlaces = linkContentHandler.getLinks();
  }

  public List<Entry<String, Integer>> contador() throws IOException, TikaException {
    String[] parts = this.contenido.split(" ");
    Map<String, Integer> map = new HashMap<String, Integer>();
    for (String w : parts) {
      final String word = w.toLowerCase();
      Integer n = map.get(word);
      n = (n == null) ? 1 : ++n;
      if (Pattern.matches("[a-zA-Z\\u00C0-\\u024F\\u1E00-\\u1EFF]+", word))
        map.put(word, n);
    }

    Set<Entry<String, Integer>> entries = map.entrySet();
    Comparator<Entry<String, Integer>> valueComparator = new Comparator<Entry<String, Integer>>() {
      @Override
      public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
        Integer v1 = e1.getValue();
        Integer v2 = e2.getValue();
        return v2.compareTo(v1);
      }
    };

    List<Entry<String, Integer>> orderedList = new ArrayList<Entry<String, Integer>>(entries);

    Collections.sort(orderedList, valueComparator);
    return orderedList;
  }

  public List<Entry<String, Integer>> contador(String analyzerType) throws IOException, TikaException {

    Analyzer analyzer;
    switch (analyzerType) {
      case "whiteAnalyzer":
        analyzer = new WhitespaceAnalyzer();
        break;
      case "simpleAnalyzer":
        analyzer = new SimpleAnalyzer();
        break;
      case "stopAnalyzer":
        analyzer = new StopAnalyzer();
        break;
      case "spanishAnalyzer":
        analyzer = new SpanishAnalyzer();
        break;
      case "customAnalyzer":
        System.out.println("CUSTOM ANALYZER");
        analyzer = new Analyzer() {
          @Override
          protected TokenStreamComponents createComponents(String fieldName) {
            try {

              InputStream affixStream = new FileInputStream("P2/dictionaries/es.aff");
              InputStream dictStream = new FileInputStream("P2/dictionaries/es.dic");
              Directory directorioTemp = FSDirectory.open(Paths.get("/temp"));
              Dictionary dic = new Dictionary(directorioTemp, "temporalFile", affixStream, dictStream);
              Tokenizer source = new UAX29URLEmailTokenizer();
              TokenStream result = new StandardFilter(source);
              result = new LowerCaseFilter(result);
              result = new HunspellStemFilter(result, dic, true, true);

              return new TokenStreamComponents(source, result);
            } catch (FileNotFoundException e) {
              e.printStackTrace();
            } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (ParseException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            return null;
          }
        };
      default:
        analyzer = new StandardAnalyzer();
        break;
    }

    TokenStream stream = analyzer.tokenStream(null, new StringReader(contenido));
    CharTermAttribute cAtt = stream.getAttribute(CharTermAttribute.class);

    stream.reset();

    ArrayList<String> parts = new ArrayList<String>();
    while (stream.incrementToken()) {
      parts.add(cAtt.toString());
    }
    stream.end();
    analyzer.close();

    Map<String, Integer> map = new HashMap<String, Integer>();
    for (String w : parts) {
      final String word = w.toLowerCase();
      Integer n = map.get(word);
      n = (n == null) ? 1 : ++n;
      if (Pattern.matches("[a-zA-Z\\u00C0-\\u024F\\u1E00-\\u1EFF]+", word))
        map.put(word, n);
    }

    Set<Entry<String, Integer>> entries = map.entrySet();
    Comparator<Entry<String, Integer>> valueComparator = new Comparator<Entry<String, Integer>>() {
      @Override
      public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
        Integer v1 = e1.getValue();
        Integer v2 = e2.getValue();
        return v2.compareTo(v1);
      }
    };

    List<Entry<String, Integer>> orderedList = new ArrayList<Entry<String, Integer>>(entries);

    Collections.sort(orderedList, valueComparator);
    return orderedList;
  }
}