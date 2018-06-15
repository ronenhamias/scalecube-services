package io.scalecube.services.benchmarks;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.SerializedString;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

@Fork(1)
@Threads(Threads.MAX)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@State(Scope.Benchmark)
public class JsonParserBenchmark {

  private static final String INPUT;
  public static final SerializedString QUALIFIER = new SerializedString("q");
  public static final SerializedString TYPE = new SerializedString("_type");

  static {
    INPUT =
        "{" +
            "\"q\":\"/hello/goodbye\"," +
            "\"_type\":\"pojo.class\"," +
            "\"data\":" + getString() + "," +
            "\"unknown\":[100,90,85]" +
            "}";
  }

  private static String getString() {
    return "[\n" +
        "   {\n" +
        "      \"_id\": \"5b23c71a5b3c28b86e3881a6\",\n" +
        "      \"index\": 0,\n" +
        "      \"guid\": \"64ec1532-1640-42e3-a4db-c1e929a7f241\",\n" +
        "      \"isActive\": true,\n" +
        "      \"balance\": \"$1,001.51\",\n" +
        "      \"picture\": \"http://placehold.it/32x32\",\n" +
        "      \"age\": 34,\n" +
        "      \"eyeColor\": \"green\",\n" +
        "      \"name\": \"Candace Burke\",\n" +
        "      \"gender\": \"female\",\n" +
        "      \"company\": \"OVERPLEX\",\n" +
        "      \"email\": \"candaceburke@overplex.com\",\n" +
        "      \"phone\": \"+1 (892) 461-3505\",\n" +
        "      \"address\": \"482 Abbey Court, Elfrida, Palau, 1298\",\n" +
        "      \"about\": \"Amet aute adipisicing ad anim occaecat consectetur eiusmod cillum. Anim ipsum officia proident et. Veniam tempor officia do laborum ullamco ad adipisicing ea. Cillum enim magna commodo laboris qui laboris. Exercitation in aute irure sit est nostrud.\\r\\n\",\n"
        +
        "      \"registered\": \"2018-05-01T05:18:01 -03:00\",\n" +
        "      \"latitude\": -77.24721,\n" +
        "      \"longitude\": -173.416662,\n" +
        "      \"tags\": [\n" +
        "         \"commodo\",\n" +
        "         \"culpa\",\n" +
        "         \"mollit\",\n" +
        "         \"id\",\n" +
        "         \"mollit\",\n" +
        "         \"magna\",\n" +
        "         \"nostrud\"\n" +
        "      ],\n" +
        "      \"friends\": [\n" +
        "         {\n" +
        "            \"id\": 0,\n" +
        "            \"name\": \"Hebert Bauer\"\n" +
        "         },\n" +
        "         {\n" +
        "            \"id\": 1,\n" +
        "            \"name\": \"Esmeralda Figueroa\"\n" +
        "         },\n" +
        "         {\n" +
        "            \"id\": 2,\n" +
        "            \"name\": \"Newman Faulkner\"\n" +
        "         }\n" +
        "      ],\n" +
        "      \"greeting\": \"Hello, Candace Burke! You have 4 unread messages.\",\n" +
        "      \"favoriteFruit\": \"apple\"\n" +
        "   },\n" +
        "   {\n" +
        "      \"_id\": \"5b23c71ab73372ab9a955141\",\n" +
        "      \"index\": 1,\n" +
        "      \"guid\": \"cf565bca-dc0f-4f5b-83dd-77720816e68c\",\n" +
        "      \"isActive\": false,\n" +
        "      \"balance\": \"$3,113.21\",\n" +
        "      \"picture\": \"http://placehold.it/32x32\",\n" +
        "      \"age\": 28,\n" +
        "      \"eyeColor\": \"blue\",\n" +
        "      \"name\": \"Mayra Ward\",\n" +
        "      \"gender\": \"female\",\n" +
        "      \"company\": \"LOCAZONE\",\n" +
        "      \"email\": \"mayraward@locazone.com\",\n" +
        "      \"phone\": \"+1 (912) 590-3823\",\n" +
        "      \"address\": \"387 Neptune Court, Homeworth, American Samoa, 8628\",\n" +
        "      \"about\": \"Sint sunt laboris ullamco mollit pariatur fugiat aliquip reprehenderit cupidatat aliqua incididunt cupidatat enim. Esse laborum non ea deserunt Lorem tempor Lorem. Excepteur sint Lorem ea ea magna sint duis pariatur et tempor. Anim aliquip dolor officia Lorem velit enim officia ad. Fugiat do cillum consequat eiusmod minim in ut occaecat aliquip veniam.\\r\\n\",\n"
        +
        "      \"registered\": \"2014-12-22T01:22:22 -02:00\",\n" +
        "      \"latitude\": -32.971092,\n" +
        "      \"longitude\": 138.278247,\n" +
        "      \"tags\": [\n" +
        "         \"nostrud\",\n" +
        "         \"et\",\n" +
        "         \"ad\",\n" +
        "         \"id\",\n" +
        "         \"et\",\n" +
        "         \"pariatur\",\n" +
        "         \"sunt\"\n" +
        "      ],\n" +
        "      \"friends\": [\n" +
        "         {\n" +
        "            \"id\": 0,\n" +
        "            \"name\": \"Morgan Holder\"\n" +
        "         },\n" +
        "         {\n" +
        "            \"id\": 1,\n" +
        "            \"name\": \"Julia Carrillo\"\n" +
        "         },\n" +
        "         {\n" +
        "            \"id\": 2,\n" +
        "            \"name\": \"Dolly Marsh\"\n" +
        "         }\n" +
        "      ],\n" +
        "      \"greeting\": \"Hello, Mayra Ward! You have 1 unread messages.\",\n" +
        "      \"favoriteFruit\": \"banana\"\n" +
        "   },\n" +
        "   {\n" +
        "      \"_id\": \"5b23c71a0da99ae5df1f3ceb\",\n" +
        "      \"index\": 2,\n" +
        "      \"guid\": \"af2cb922-7513-4937-9e52-1af1fcbe47a8\",\n" +
        "      \"isActive\": false,\n" +
        "      \"balance\": \"$1,563.34\",\n" +
        "      \"picture\": \"http://placehold.it/32x32\",\n" +
        "      \"age\": 29,\n" +
        "      \"eyeColor\": \"brown\",\n" +
        "      \"name\": \"Stuart Stein\",\n" +
        "      \"gender\": \"male\",\n" +
        "      \"company\": \"ZEPITOPE\",\n" +
        "      \"email\": \"stuartstein@zepitope.com\",\n" +
        "      \"phone\": \"+1 (867) 514-2928\",\n" +
        "      \"address\": \"843 Seaview Court, Lithium, North Carolina, 4385\",\n" +
        "      \"about\": \"Qui ea eu minim excepteur. Aliqua ipsum cillum magna culpa labore mollit incididunt ea qui duis. Minim aliquip eu culpa ut adipisicing pariatur esse aliqua. Nisi exercitation id ad reprehenderit laborum tempor excepteur Lorem esse eu. Cillum et est id eiusmod nisi commodo sint aliqua nostrud labore nisi enim adipisicing. Reprehenderit consectetur culpa dolore ea excepteur eiusmod aliqua labore aliquip consequat esse qui ullamco.\\r\\n\",\n"
        +
        "      \"registered\": \"2016-09-10T10:38:16 -03:00\",\n" +
        "      \"latitude\": 35.802147,\n" +
        "      \"longitude\": -31.958442,\n" +
        "      \"tags\": [\n" +
        "         \"ipsum\",\n" +
        "         \"consequat\",\n" +
        "         \"qui\",\n" +
        "         \"velit\",\n" +
        "         \"ad\",\n" +
        "         \"culpa\",\n" +
        "         \"sint\"\n" +
        "      ],\n" +
        "      \"friends\": [\n" +
        "         {\n" +
        "            \"id\": 0,\n" +
        "            \"name\": \"Savage Fitzgerald\"\n" +
        "         },\n" +
        "         {\n" +
        "            \"id\": 1,\n" +
        "            \"name\": \"Mcfadden Whitaker\"\n" +
        "         },\n" +
        "         {\n" +
        "            \"id\": 2,\n" +
        "            \"name\": \"Collier Sargent\"\n" +
        "         }\n" +
        "      ],\n" +
        "      \"greeting\": \"Hello, Stuart Stein! You have 6 unread messages.\",\n" +
        "      \"favoriteFruit\": \"banana\"\n" +
        "   },\n" +
        "   {\n" +
        "      \"_id\": \"5b23c71a7ac85274a161a732\",\n" +
        "      \"index\": 3,\n" +
        "      \"guid\": \"a8c4bdd2-aee4-4a71-8d51-27e3525e95bc\",\n" +
        "      \"isActive\": false,\n" +
        "      \"balance\": \"$2,375.42\",\n" +
        "      \"picture\": \"http://placehold.it/32x32\",\n" +
        "      \"age\": 35,\n" +
        "      \"eyeColor\": \"blue\",\n" +
        "      \"name\": \"Vivian Cunningham\",\n" +
        "      \"gender\": \"female\",\n" +
        "      \"company\": \"VALREDA\",\n" +
        "      \"email\": \"viviancunningham@valreda.com\",\n" +
        "      \"phone\": \"+1 (953) 491-3580\",\n" +
        "      \"address\": \"961 Schenck Place, Kerby, California, 727\",\n" +
        "      \"about\": \"Aliquip ex Lorem consectetur incididunt pariatur sint anim duis Lorem. Quis magna fugiat qui consectetur aliqua. Ipsum laboris laborum sit nulla dolor dolor incididunt commodo esse ex. Duis sit est id et minim elit excepteur dolor laborum magna amet nulla amet. Occaecat dolor ullamco culpa nostrud ut ad laboris reprehenderit reprehenderit elit nostrud fugiat. Mollit eu fugiat cillum deserunt dolore reprehenderit officia deserunt cupidatat. Esse ex velit non occaecat nisi ipsum non Lorem Lorem consequat tempor.\\r\\n\",\n"
        +
        "      \"registered\": \"2016-03-19T09:19:40 -02:00\",\n" +
        "      \"latitude\": -42.74136,\n" +
        "      \"longitude\": -64.042546,\n" +
        "      \"tags\": [\n" +
        "         \"dolor\",\n" +
        "         \"dolor\",\n" +
        "         \"non\",\n" +
        "         \"nulla\",\n" +
        "         \"ut\",\n" +
        "         \"nostrud\",\n" +
        "         \"quis\"\n" +
        "      ],\n" +
        "      \"friends\": [\n" +
        "         {\n" +
        "            \"id\": 0,\n" +
        "            \"name\": \"Winters Raymond\"\n" +
        "         },\n" +
        "         {\n" +
        "            \"id\": 1,\n" +
        "            \"name\": \"Sybil Flynn\"\n" +
        "         },\n" +
        "         {\n" +
        "            \"id\": 2,\n" +
        "            \"name\": \"Stanley Brooks\"\n" +
        "         }\n" +
        "      ],\n" +
        "      \"greeting\": \"Hello, Vivian Cunningham! You have 4 unread messages.\",\n" +
        "      \"favoriteFruit\": \"apple\"\n" +
        "   },\n" +
        "   {\n" +
        "      \"_id\": \"5b23c71a8c5fe452748f49fc\",\n" +
        "      \"index\": 4,\n" +
        "      \"guid\": \"896fcc1c-9d21-46c1-abf5-5bc79feb7ee3\",\n" +
        "      \"isActive\": false,\n" +
        "      \"balance\": \"$2,020.10\",\n" +
        "      \"picture\": \"http://placehold.it/32x32\",\n" +
        "      \"age\": 24,\n" +
        "      \"eyeColor\": \"brown\",\n" +
        "      \"name\": \"Mcfarland Larson\",\n" +
        "      \"gender\": \"male\",\n" +
        "      \"company\": \"COMTRACT\",\n" +
        "      \"email\": \"mcfarlandlarson@comtract.com\",\n" +
        "      \"phone\": \"+1 (934) 504-3239\",\n" +
        "      \"address\": \"579 Chester Avenue, Orin, Hawaii, 3782\",\n" +
        "      \"about\": \"Fugiat in excepteur eiusmod cupidatat veniam eiusmod et duis quis ipsum. Aute quis exercitation laboris ad ipsum sint tempor. Labore exercitation ad aliqua culpa reprehenderit laboris sunt fugiat. Laborum sit commodo ipsum ea ex ad irure aute cillum et. Nisi ea aute et nulla velit incididunt laborum nisi. Ad proident ad enim elit officia elit ullamco reprehenderit cillum anim ipsum.\\r\\n\",\n"
        +
        "      \"registered\": \"2017-09-15T09:02:53 -03:00\",\n" +
        "      \"latitude\": -8.8004,\n" +
        "      \"longitude\": -98.774113,\n" +
        "      \"tags\": [\n" +
        "         \"dolor\",\n" +
        "         \"labore\",\n" +
        "         \"incididunt\",\n" +
        "         \"tempor\",\n" +
        "         \"irure\",\n" +
        "         \"aute\",\n" +
        "         \"enim\"\n" +
        "      ],\n" +
        "      \"friends\": [\n" +
        "         {\n" +
        "            \"id\": 0,\n" +
        "            \"name\": \"Mcintosh Harmon\"\n" +
        "         },\n" +
        "         {\n" +
        "            \"id\": 1,\n" +
        "            \"name\": \"Haley Harrell\"\n" +
        "         },\n" +
        "         {\n" +
        "            \"id\": 2,\n" +
        "            \"name\": \"Antoinette Rosario\"\n" +
        "         }\n" +
        "      ],\n" +
        "      \"greeting\": \"Hello, Mcfarland Larson! You have 6 unread messages.\",\n" +
        "      \"favoriteFruit\": \"banana\"\n" +
        "   }\n" +
        "]";
  }

  static final JsonFactory JSON_FACTORY = new JsonFactory();

  public static void main(String[] args) throws IOException {
    JsonParser jsonParser = JSON_FACTORY.createParser(new StringReader(INPUT));
    jsonParser.nextToken(); // {
    //
    // System.err.println(jsonParser.nextFieldName() + " : " + jsonParser.nextTextValue()); // "q"
    // System.err.println(jsonParser.nextFieldName() + " : " + jsonParser.nextTextValue()); // "_type"

    // System.err.println(jsonParser.nextFieldName());// data
    // jsonParser.skipChildren();
    // jsonParser.nextToken();


    for (int i = 0; i < 10000; i++) {
      System.out.println(jsonParser.currentToken());
      System.out.println("jsonParser.nextFieldName() = " + jsonParser.nextFieldName());
      jsonParser.nextToken();
      // System.out.println(jsonParser.);
      // System.err.println(jsonParser.nextFieldName() + " : " + jsonParser.nextTextValue());
    }



    // System.err.println(jsonParser.nextFieldName() + " : " + jsonParser.nextTextValue());
    // System.err.println(jsonParser.nextFieldName() + " : " + jsonParser.nextTextValue());
    // System.err.println(jsonParser.nextFieldName() + " : " + jsonParser.nextTextValue());
    // System.err.println(jsonParser.nextFieldName() + " : " + jsonParser.nextTextValue());
    // System.err.println(jsonParser.nextFieldName() + " : " + jsonParser.nextTextValue());

    //
    // JsonToken jsonToken = null;
    // while ((jsonToken = jsonParser.nextToken()) != null) {
    // System.out.println(jsonToken.asString());
    // }
  }

  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Benchmark
  public void testJsonParser() throws IOException {
    JsonParser jsonParser = JSON_FACTORY.createParser(new StringReader(INPUT));
    jsonParser.nextToken(); // {

    jsonParser.nextFieldName(); // "q"
    jsonParser.nextTextValue();

    jsonParser.nextFieldName(); // "_type"
    jsonParser.nextTextValue();

    jsonParser.nextFieldName();// data
    jsonParser.skipChildren();

    JsonToken jsonToken = null;

    while ((jsonToken = jsonParser.nextToken()) != null) {

    }


    // String fieldName = jsonParser.nextFieldName();// unknown
    // System.out.println(fieldName);
    // String s = jsonParser.nextToken().asString();
    // System.out.println(s);
    // while (true) {
    // // jsonParser.nextToken() != JsonToken.END_OBJECT
    //
    // // get the current token
    // String fieldname = jsonParser.getCurrentName();
    //
    // System.out.println(fieldname);
    // }
  }
}
