public class Doc {
  public String title;
  public String content;
  public String year;
  public String eid;
  public String[] authors;
  public String[] keywords;

  Doc(String title, String content, String year, String[] authors, String[] keywords, String eid) {
    this.title = title;
    this.content = content;
    this.year = year;
    this.authors = authors;
    this.keywords = keywords;
    this.eid = eid;
  }
}
