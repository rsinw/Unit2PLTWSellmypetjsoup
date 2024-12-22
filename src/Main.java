import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String baseReviewUrl = "https://www.trustpilot.com/review/www.amazon.com?page=";
        int totalPages = 100;
        String outputFileName = "reviews.csv";

        try (FileWriter writer = new FileWriter(outputFileName)) {
            writer.write("\"Reviewer Name\",\"Review\",\"Number of Stars\",\"Date of Review\"\n");
            int reviewCount = 0;

            for (int page = 1; page <= totalPages; page++) {
                String url = baseReviewUrl + page;

                Connection connection = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                                   "Chrome/115.0.0.0 Safari/537.36")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.9")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Referer", "https://www.trustpilot.com/")
                        .timeout(10000)
                        .followRedirects(true);

                // Sleep to mimic human browsing
                try {
                    Thread.sleep((long) (2000 + Math.random() * 3000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Document doc = connection.get();
                System.out.println("HTML Content for Page " + page + ":");
                System.out.println(doc.html());

                // Select each review article
                Elements reviews = doc.select("article[data-service-review-card-paper]");
                if (reviews.isEmpty()) {
                    System.out.println("No reviews found on page " + page + ". Possibly dynamically loaded or no reviews present.");
                    break;
                }

                for (Element review : reviews) {
                    // Extract reviewer name
                    String reviewerName = review.select("span[data-consumer-name-typography]").text().replaceAll("\"", "\"\"");
                    if (reviewerName.isEmpty()) reviewerName = "N/A";

                    // Extract review text
                    String reviewText = review.select("p[data-service-review-text-typography]").text().replaceAll("\"", "\"\"");
                    if (reviewText.isEmpty()) reviewText = "N/A";

                    // Extract stars (e.g. alt="Rated 1 out of 5 stars")
                    String stars = review.select("div[data-service-review-rating] img").attr("alt").replaceAll("\"", "\"\"");
                    if (stars.isEmpty()) stars = "N/A";

                    // Extract date of review (the "Date of experience")
                    String date = review.select("p[data-service-review-date-of-experience-typography]").text().replaceAll("\"", "\"\"");
                    if (date.isEmpty()) date = "N/A";


                    // Write CSV line
                    writer.write("\"" + reviewerName + "\",\""
                            + reviewText + "\",\""
                            + stars + "\",\""
                            + date + "\"\n");
                    reviewCount++;
                }
            }

            System.out.println("Scraping completed. Total reviews scraped: " + reviewCount);
            System.out.println("Data saved to: " + outputFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
