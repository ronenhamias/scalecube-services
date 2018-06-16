package io.scalecube.services.benchmarks.jsonmapping;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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
import org.openjdk.jmh.infra.Blackhole;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Fork(1)
@Threads(Threads.MAX)
@Warmup(iterations = 1)
@Measurement(iterations = 2)
@State(Scope.Benchmark)
public class JsonParserBenchmark {

  private static final String INPUT;
  private static final ByteBuf BYTE_BUF;
  private static final ServiceMessageMapper MAPPER = new ServiceMessageMapper();
  private static final ServiceMessageMapperLookForOnlyQualifier MAPPER_LOOK_FOR_ONLY_QUALIFIER =
      new ServiceMessageMapperLookForOnlyQualifier();
  private static final LegacyServiceMessageMapper LEGACY_MAPPER = new LegacyServiceMessageMapper();

  static {
    INPUT =
        "{" +
            "\"q\":\"/hello/goodbye\"," +
            "\"dataType\":\"pojo.class\"," +
            "\"data\":" + getObjectData() + "," +
            "\"unknown\":\"someValue\"" +
            "}";
    BYTE_BUF = Unpooled.copiedBuffer(INPUT.getBytes());
  }


  // public static void main(String[] args) {
  // ServiceMessage2 message = LEGACY_MAPPER.decode(BYTE_BUF);
  // System.out.println(message.qualifier());
  // System.out.println(message.dataType());
  // System.out.println(message.headers());
  // }

  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Benchmark
  public void testJsonParser(Blackhole bh) {
    bh.consume(MAPPER.decode(BYTE_BUF));
  }

  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Benchmark
  public void testJsonParserLookForOnlyQualifier(Blackhole bh) {
    bh.consume(MAPPER_LOOK_FOR_ONLY_QUALIFIER.decode(BYTE_BUF));
  }

  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Benchmark
  public void testLegacyJsonParser(Blackhole bh) {
    bh.consume(LEGACY_MAPPER.decode(BYTE_BUF));
  }

  private static String getArrayData() {
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

  private static String getObjectData() {
    return "{\n" + "\"items\": [\n" + "{\n" + "\"index\": 1,\n" + "\"index_start_at\": 56,\n" + "\"integer\": 38,\n"
        + "\"float\": 15.6384,\n" + "\"name\": \"Terry\",\n" + "\"surname\": \"Mitchell\",\n"
        + "\"fullname\": \"William Briggs\",\n" + "\"email\": \"max@lindsay.lr\",\n" + "\"bool\": true\n" + "},\n"
        + "{\n" + "\"index\": 2,\n" + "\"index_start_at\": 57,\n" + "\"integer\": 29,\n" + "\"float\": 17.017,\n"
        + "\"name\": \"Glenda\",\n" + "\"surname\": \"Parsons\",\n" + "\"fullname\": \"Chris Sherrill\",\n"
        + "\"email\": \"bob@gould.ba\",\n" + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 3,\n"
        + "\"index_start_at\": 58,\n" + "\"integer\": 6,\n" + "\"float\": 17.1784,\n" + "\"name\": \"Bradley\",\n"
        + "\"surname\": \"Stephens\",\n" + "\"fullname\": \"Pauline Goldstein\",\n"
        + "\"email\": \"edgar@watson.bz\",\n" + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 4,\n"
        + "\"index_start_at\": 59,\n" + "\"integer\": 27,\n" + "\"float\": 10.084,\n" + "\"name\": \"Gretchen\",\n"
        + "\"surname\": \"Henry\",\n" + "\"fullname\": \"Vivian Rich\",\n" + "\"email\": \"sue@parks.af\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 5,\n" + "\"index_start_at\": 60,\n" + "\"integer\": 36,\n"
        + "\"float\": 17.1539,\n" + "\"name\": \"Sheryl\",\n" + "\"surname\": \"Johnston\",\n"
        + "\"fullname\": \"Julie McLean\",\n" + "\"email\": \"donna@barton.mx\",\n" + "\"bool\": true\n" + "},\n"
        + "{\n" + "\"index\": 6,\n" + "\"index_start_at\": 61,\n" + "\"integer\": 3,\n" + "\"float\": 11.8371,\n"
        + "\"name\": \"Geoffrey\",\n" + "\"surname\": \"Tyler\",\n" + "\"fullname\": \"Melanie Liu\",\n"
        + "\"email\": \"lori@justice.kn\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 7,\n"
        + "\"index_start_at\": 62,\n" + "\"integer\": 25,\n" + "\"float\": 13.1523,\n" + "\"name\": \"Brian\",\n"
        + "\"surname\": \"Scott\",\n" + "\"fullname\": \"Nicholas Rodgers\",\n" + "\"email\": \"derek@garrett.ht\",\n"
        + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 8,\n" + "\"index_start_at\": 63,\n" + "\"integer\": 24,\n"
        + "\"float\": 18.8396,\n" + "\"name\": \"Mark\",\n" + "\"surname\": \"Maynard\",\n"
        + "\"fullname\": \"Greg Freeman\",\n" + "\"email\": \"billie@coleman.eg\",\n" + "\"bool\": false\n" + "},\n"
        + "{\n" + "\"index\": 9,\n" + "\"index_start_at\": 64,\n" + "\"integer\": 6,\n" + "\"float\": 15.1247,\n"
        + "\"name\": \"Nancy\",\n" + "\"surname\": \"Fisher\",\n" + "\"fullname\": \"Donna Koch\",\n"
        + "\"email\": \"bob@yates.km\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 10,\n"
        + "\"index_start_at\": 65,\n" + "\"integer\": 39,\n" + "\"float\": 13.2556,\n" + "\"name\": \"Ellen\",\n"
        + "\"surname\": \"Pickett\",\n" + "\"fullname\": \"George Daniel\",\n" + "\"email\": \"frances@conrad.sb\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 11,\n" + "\"index_start_at\": 66,\n" + "\"integer\": 46,\n"
        + "\"float\": 17.572,\n" + "\"name\": \"Caroline\",\n" + "\"surname\": \"Riley\",\n"
        + "\"fullname\": \"Mary Dale\",\n" + "\"email\": \"gene@bolton.hn\",\n" + "\"bool\": false\n" + "},\n" + "{\n"
        + "\"index\": 12,\n" + "\"index_start_at\": 67,\n" + "\"integer\": 11,\n" + "\"float\": 10.571,\n"
        + "\"name\": \"Tamara\",\n" + "\"surname\": \"Stanley\",\n" + "\"fullname\": \"Danielle Field\",\n"
        + "\"email\": \"pat@garrett.gd\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 13,\n"
        + "\"index_start_at\": 68,\n" + "\"integer\": 39,\n" + "\"float\": 11.0115,\n" + "\"name\": \"Leo\",\n"
        + "\"surname\": \"McMillan\",\n" + "\"fullname\": \"Sherri Ellis\",\n" + "\"email\": \"jon@mayo.ng\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 14,\n" + "\"index_start_at\": 69,\n" + "\"integer\": 23,\n"
        + "\"float\": 12.4139,\n" + "\"name\": \"Brandon\",\n" + "\"surname\": \"Thornton\",\n"
        + "\"fullname\": \"Anne Melton\",\n" + "\"email\": \"ted@lang.gs\",\n" + "\"bool\": false\n" + "},\n" + "{\n"
        + "\"index\": 15,\n" + "\"index_start_at\": 70,\n" + "\"integer\": 20,\n" + "\"float\": 11.2799,\n"
        + "\"name\": \"Gregory\",\n" + "\"surname\": \"Anderson\",\n" + "\"fullname\": \"Holly Friedman\",\n"
        + "\"email\": \"jerome@sullivan.ph\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 16,\n"
        + "\"index_start_at\": 71,\n" + "\"integer\": 28,\n" + "\"float\": 13.5341,\n" + "\"name\": \"Judith\",\n"
        + "\"surname\": \"Barton\",\n" + "\"fullname\": \"Amy Schwartz\",\n" + "\"email\": \"leroy@grant.am\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 17,\n" + "\"index_start_at\": 72,\n" + "\"integer\": 22,\n"
        + "\"float\": 18.3354,\n" + "\"name\": \"Luis\",\n" + "\"surname\": \"Nance\",\n"
        + "\"fullname\": \"Joan Bowling\",\n" + "\"email\": \"calvin@aycock.sa\",\n" + "\"bool\": false\n" + "},\n"
        + "{\n" + "\"index\": 18,\n" + "\"index_start_at\": 73,\n" + "\"integer\": 4,\n" + "\"float\": 12.3744,\n"
        + "\"name\": \"Eric\",\n" + "\"surname\": \"Albright\",\n" + "\"fullname\": \"Hazel Moon\",\n"
        + "\"email\": \"ted@james.pn\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 19,\n"
        + "\"index_start_at\": 74,\n" + "\"integer\": 40,\n" + "\"float\": 18.7692,\n" + "\"name\": \"Erin\",\n"
        + "\"surname\": \"Richardson\",\n" + "\"fullname\": \"Joann Hawkins\",\n" + "\"email\": \"beth@melton.gd\",\n"
        + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 20,\n" + "\"index_start_at\": 75,\n" + "\"integer\": 39,\n"
        + "\"float\": 15.9295,\n" + "\"name\": \"Lois\",\n" + "\"surname\": \"Sawyer\",\n"
        + "\"fullname\": \"Ken Riddle\",\n" + "\"email\": \"malcolm@fisher.gb\",\n" + "\"bool\": false\n" + "},\n"
        + "{\n" + "\"index\": 21,\n" + "\"index_start_at\": 76,\n" + "\"integer\": 44,\n" + "\"float\": 18.3944,\n"
        + "\"name\": \"Jane\",\n" + "\"surname\": \"Sellers\",\n" + "\"fullname\": \"Walter McKenzie\",\n"
        + "\"email\": \"ronald@byers.lb\",\n" + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 22,\n"
        + "\"index_start_at\": 77,\n" + "\"integer\": 22,\n" + "\"float\": 15.3557,\n" + "\"name\": \"Neal\",\n"
        + "\"surname\": \"Pitts\",\n" + "\"fullname\": \"Todd Fischer\",\n" + "\"email\": \"greg@pappas.tf\",\n"
        + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 23,\n" + "\"index_start_at\": 78,\n" + "\"integer\": 20,\n"
        + "\"float\": 19.6344,\n" + "\"name\": \"Allen\",\n" + "\"surname\": \"Fletcher\",\n"
        + "\"fullname\": \"Sandra Crane\",\n" + "\"email\": \"stephen@tyson.om\",\n" + "\"bool\": true\n" + "},\n"
        + "{\n" + "\"index\": 24,\n" + "\"index_start_at\": 79,\n" + "\"integer\": 15,\n" + "\"float\": 15.8161,\n"
        + "\"name\": \"Jose\",\n" + "\"surname\": \"Coley\",\n" + "\"fullname\": \"Randall Yates\",\n"
        + "\"email\": \"frances@roy.mr\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 25,\n"
        + "\"index_start_at\": 80,\n" + "\"integer\": 37,\n" + "\"float\": 10.7656,\n" + "\"name\": \"Christine\",\n"
        + "\"surname\": \"Kaplan\",\n" + "\"fullname\": \"Holly Chandler\",\n" + "\"email\": \"samuel@wrenn.kw\",\n"
        + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 26,\n" + "\"index_start_at\": 81,\n" + "\"integer\": 43,\n"
        + "\"float\": 14.9081,\n" + "\"name\": \"Barry\",\n" + "\"surname\": \"Woods\",\n"
        + "\"fullname\": \"Nathan Sherrill\",\n" + "\"email\": \"joe@beatty.ai\",\n" + "\"bool\": true\n" + "},\n"
        + "{\n" + "\"index\": 27,\n" + "\"index_start_at\": 82,\n" + "\"integer\": 46,\n" + "\"float\": 17.4366,\n"
        + "\"name\": \"Dianne\",\n" + "\"surname\": \"Barr\",\n" + "\"fullname\": \"Kenneth Lancaster\",\n"
        + "\"email\": \"lester@parsons.mo\",\n" + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 28,\n"
        + "\"index_start_at\": 83,\n" + "\"integer\": 1,\n" + "\"float\": 18.039,\n" + "\"name\": \"Mike\",\n"
        + "\"surname\": \"Knowles\",\n" + "\"fullname\": \"Edna Chang\",\n" + "\"email\": \"barry@davenport.de\",\n"
        + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 29,\n" + "\"index_start_at\": 84,\n" + "\"integer\": 22,\n"
        + "\"float\": 18.8122,\n" + "\"name\": \"Billie\",\n" + "\"surname\": \"Robinson\",\n"
        + "\"fullname\": \"Kate Mueller\",\n" + "\"email\": \"eva@cooper.com\",\n" + "\"bool\": true\n" + "},\n" + "{\n"
        + "\"index\": 30,\n" + "\"index_start_at\": 85,\n" + "\"integer\": 27,\n" + "\"float\": 15.5921,\n"
        + "\"name\": \"Anne\",\n" + "\"surname\": \"Cooke\",\n" + "\"fullname\": \"Tammy Lassiter\",\n"
        + "\"email\": \"wanda@koch.aw\",\n" + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 31,\n"
        + "\"index_start_at\": 86,\n" + "\"integer\": 5,\n" + "\"float\": 16.9506,\n" + "\"name\": \"Ross\",\n"
        + "\"surname\": \"Terrell\",\n" + "\"fullname\": \"Donna Kearney\",\n" + "\"email\": \"leo@phillips.tt\",\n"
        + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 32,\n" + "\"index_start_at\": 87,\n" + "\"integer\": 33,\n"
        + "\"float\": 14.4328,\n" + "\"name\": \"Alexander\",\n" + "\"surname\": \"Hardy\",\n"
        + "\"fullname\": \"Zachary Richards\",\n" + "\"email\": \"kristina@dunlap.tn\",\n" + "\"bool\": false\n"
        + "},\n" + "{\n" + "\"index\": 33,\n" + "\"index_start_at\": 88,\n" + "\"integer\": 40,\n"
        + "\"float\": 18.5865,\n" + "\"name\": \"Hazel\",\n" + "\"surname\": \"Gordon\",\n"
        + "\"fullname\": \"Denise Zhang\",\n" + "\"email\": \"kathy@snow.om\",\n" + "\"bool\": true\n" + "},\n" + "{\n"
        + "\"index\": 34,\n" + "\"index_start_at\": 89,\n" + "\"integer\": 21,\n" + "\"float\": 13.3732,\n"
        + "\"name\": \"Carole\",\n" + "\"surname\": \"West\",\n" + "\"fullname\": \"Justin Ball\",\n"
        + "\"email\": \"angela@rubin.lv\",\n" + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 35,\n"
        + "\"index_start_at\": 90,\n" + "\"integer\": 32,\n" + "\"float\": 12.7695,\n" + "\"name\": \"Robin\",\n"
        + "\"surname\": \"Pierce\",\n" + "\"fullname\": \"Darlene Hawkins\",\n" + "\"email\": \"rita@harper.ru\",\n"
        + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 36,\n" + "\"index_start_at\": 91,\n" + "\"integer\": 17,\n"
        + "\"float\": 13.4603,\n" + "\"name\": \"Allison\",\n" + "\"surname\": \"Palmer\",\n"
        + "\"fullname\": \"Heather Coates\",\n" + "\"email\": \"joan@gibbons.so\",\n" + "\"bool\": false\n" + "},\n"
        + "{\n" + "\"index\": 37,\n" + "\"index_start_at\": 92,\n" + "\"integer\": 14,\n" + "\"float\": 14.4627,\n"
        + "\"name\": \"Joel\",\n" + "\"surname\": \"Carlton\",\n" + "\"fullname\": \"Ethel Conrad\",\n"
        + "\"email\": \"leo@barrett.tl (changed from tp)\",\n" + "\"bool\": true\n" + "},\n" + "{\n"
        + "\"index\": 38,\n" + "\"index_start_at\": 93,\n" + "\"integer\": 7,\n" + "\"float\": 17.2523,\n"
        + "\"name\": \"Lynn\",\n" + "\"surname\": \"Chan\",\n" + "\"fullname\": \"Mark Benson\",\n"
        + "\"email\": \"neal@cox.sn\",\n" + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 39,\n"
        + "\"index_start_at\": 94,\n" + "\"integer\": 42,\n" + "\"float\": 11.5671,\n" + "\"name\": \"Katherine\",\n"
        + "\"surname\": \"Washington\",\n" + "\"fullname\": \"Eric Robinson\",\n" + "\"email\": \"claude@huff.zw\",\n"
        + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 40,\n" + "\"index_start_at\": 95,\n" + "\"integer\": 26,\n"
        + "\"float\": 17.0187,\n" + "\"name\": \"Joanne\",\n" + "\"surname\": \"Graves\",\n"
        + "\"fullname\": \"Nicholas Ford\",\n" + "\"email\": \"evan@katz.tz\",\n" + "\"bool\": true\n" + "},\n" + "{\n"
        + "\"index\": 41,\n" + "\"index_start_at\": 96,\n" + "\"integer\": 5,\n" + "\"float\": 17.0718,\n"
        + "\"name\": \"Amy\",\n" + "\"surname\": \"Bowden\",\n" + "\"fullname\": \"Kimberly Yates\",\n"
        + "\"email\": \"cheryl@mcallister.gm\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 42,\n"
        + "\"index_start_at\": 97,\n" + "\"integer\": 12,\n" + "\"float\": 14.4043,\n" + "\"name\": \"Paige\",\n"
        + "\"surname\": \"Starr\",\n" + "\"fullname\": \"Brooke McKinney\",\n" + "\"email\": \"sidney@crane.gu\",\n"
        + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 43,\n" + "\"index_start_at\": 98,\n" + "\"integer\": 22,\n"
        + "\"float\": 14.4277,\n" + "\"name\": \"Keith\",\n" + "\"surname\": \"Knowles\",\n"
        + "\"fullname\": \"Neil Eaton\",\n" + "\"email\": \"tara@gray.br\",\n" + "\"bool\": true\n" + "},\n" + "{\n"
        + "\"index\": 44,\n" + "\"index_start_at\": 99,\n" + "\"integer\": 0,\n" + "\"float\": 18.9573,\n"
        + "\"name\": \"Allison\",\n" + "\"surname\": \"Bowling\",\n" + "\"fullname\": \"Alison Hester\",\n"
        + "\"email\": \"kristin@woodward.be\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 45,\n"
        + "\"index_start_at\": 100,\n" + "\"integer\": 25,\n" + "\"float\": 14.3542,\n" + "\"name\": \"Vickie\",\n"
        + "\"surname\": \"Gordon\",\n" + "\"fullname\": \"Eleanor Bray\",\n" + "\"email\": \"shirley@kane.ax\",\n"
        + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 46,\n" + "\"index_start_at\": 101,\n" + "\"integer\": 44,\n"
        + "\"float\": 19.4568,\n" + "\"name\": \"Gene\",\n" + "\"surname\": \"Payne\",\n"
        + "\"fullname\": \"Chris Monroe\",\n" + "\"email\": \"nina@garrett.sr\",\n" + "\"bool\": true\n" + "},\n"
        + "{\n" + "\"index\": 47,\n" + "\"index_start_at\": 102,\n" + "\"integer\": 30,\n" + "\"float\": 13.7464,\n"
        + "\"name\": \"Lawrence\",\n" + "\"surname\": \"Olsen\",\n" + "\"fullname\": \"Natalie Callahan\",\n"
        + "\"email\": \"calvin@douglas.rs\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 48,\n"
        + "\"index_start_at\": 103,\n" + "\"integer\": 2,\n" + "\"float\": 13.7488,\n" + "\"name\": \"Joann\",\n"
        + "\"surname\": \"Owen\",\n" + "\"fullname\": \"Greg Haynes\",\n" + "\"email\": \"samantha@cooke.az\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 49,\n" + "\"index_start_at\": 104,\n"
        + "\"integer\": 21,\n" + "\"float\": 13.8438,\n" + "\"name\": \"Marc\",\n" + "\"surname\": \"Curtis\",\n"
        + "\"fullname\": \"Phyllis Rowland\",\n" + "\"email\": \"diana@warren.tn\",\n" + "\"bool\": true\n" + "},\n"
        + "{\n" + "\"index\": 50,\n" + "\"index_start_at\": 105,\n" + "\"integer\": 30,\n" + "\"float\": 18.4043,\n"
        + "\"name\": \"Frances\",\n" + "\"surname\": \"Richards\",\n" + "\"fullname\": \"Janet Schwartz\",\n"
        + "\"email\": \"steven@douglas.la\",\n" + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 51,\n"
        + "\"index_start_at\": 106,\n" + "\"integer\": 11,\n" + "\"float\": 14.2978,\n" + "\"name\": \"Megan\",\n"
        + "\"surname\": \"Schultz\",\n" + "\"fullname\": \"Christopher Watts\",\n"
        + "\"email\": \"judith@lancaster.dz\",\n" + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 52,\n"
        + "\"index_start_at\": 107,\n" + "\"integer\": 39,\n" + "\"float\": 15.3694,\n" + "\"name\": \"Vernon\",\n"
        + "\"surname\": \"Pearson\",\n" + "\"fullname\": \"Richard Eason\",\n" + "\"email\": \"joseph@bender.edu\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 53,\n" + "\"index_start_at\": 108,\n"
        + "\"integer\": 21,\n" + "\"float\": 14.8276,\n" + "\"name\": \"Mary\",\n" + "\"surname\": \"Jones\",\n"
        + "\"fullname\": \"Marsha Choi\",\n" + "\"email\": \"tara@rowland.mil\",\n" + "\"bool\": true\n" + "},\n"
        + "{\n" + "\"index\": 54,\n" + "\"index_start_at\": 109,\n" + "\"integer\": 49,\n" + "\"float\": 13.7025,\n"
        + "\"name\": \"Dana\",\n" + "\"surname\": \"Desai\",\n" + "\"fullname\": \"Kathleen Hayes\",\n"
        + "\"email\": \"donna@stone.mh\",\n" + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 55,\n"
        + "\"index_start_at\": 110,\n" + "\"integer\": 46,\n" + "\"float\": 12.0892,\n" + "\"name\": \"Tracy\",\n"
        + "\"surname\": \"Galloway\",\n" + "\"fullname\": \"Alice Zhang\",\n" + "\"email\": \"lisa@hale.cv\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 56,\n" + "\"index_start_at\": 111,\n"
        + "\"integer\": 23,\n" + "\"float\": 13.4306,\n" + "\"name\": \"Jeremy\",\n" + "\"surname\": \"Elmore\",\n"
        + "\"fullname\": \"Gilbert Jones\",\n" + "\"email\": \"geraldine@wu.pf\",\n" + "\"bool\": true\n" + "},\n"
        + "{\n" + "\"index\": 57,\n" + "\"index_start_at\": 112,\n" + "\"integer\": 3,\n" + "\"float\": 17.7813,\n"
        + "\"name\": \"Shelley\",\n" + "\"surname\": \"Coates\",\n" + "\"fullname\": \"Florence Bolton\",\n"
        + "\"email\": \"molly@kearney.mg\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 58,\n"
        + "\"index_start_at\": 113,\n" + "\"integer\": 7,\n" + "\"float\": 13.5068,\n" + "\"name\": \"Jan\",\n"
        + "\"surname\": \"Lowry\",\n" + "\"fullname\": \"Wesley Allison\",\n" + "\"email\": \"lois@fisher.kp\",\n"
        + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 59,\n" + "\"index_start_at\": 114,\n" + "\"integer\": 21,\n"
        + "\"float\": 18.9973,\n" + "\"name\": \"Brent\",\n" + "\"surname\": \"Barefoot\",\n"
        + "\"fullname\": \"Jon Quinn\",\n" + "\"email\": \"alan@monroe.lu\",\n" + "\"bool\": false\n" + "},\n" + "{\n"
        + "\"index\": 60,\n" + "\"index_start_at\": 115,\n" + "\"integer\": 36,\n" + "\"float\": 11.7524,\n"
        + "\"name\": \"Lester\",\n" + "\"surname\": \"Schroeder\",\n" + "\"fullname\": \"Janet Haynes\",\n"
        + "\"email\": \"frances@may.fm\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 61,\n"
        + "\"index_start_at\": 116,\n" + "\"integer\": 49,\n" + "\"float\": 16.8496,\n" + "\"name\": \"Jean\",\n"
        + "\"surname\": \"Burgess\",\n" + "\"fullname\": \"Jordan Burnett\",\n"
        + "\"email\": \"gretchen@lindsay.fo\",\n" + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 62,\n"
        + "\"index_start_at\": 117,\n" + "\"integer\": 37,\n" + "\"float\": 12.3441,\n" + "\"name\": \"Christina\",\n"
        + "\"surname\": \"Klein\",\n" + "\"fullname\": \"Samantha Wiggins\",\n" + "\"email\": \"marian@joseph.et\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 63,\n" + "\"index_start_at\": 118,\n"
        + "\"integer\": 36,\n" + "\"float\": 12.4985,\n" + "\"name\": \"Brett\",\n" + "\"surname\": \"Spivey\",\n"
        + "\"fullname\": \"Malcolm Archer\",\n" + "\"email\": \"katherine@horn.gr\",\n" + "\"bool\": true\n" + "},\n"
        + "{\n" + "\"index\": 64,\n" + "\"index_start_at\": 119,\n" + "\"integer\": 23,\n" + "\"float\": 16.7997,\n"
        + "\"name\": \"Eileen\",\n" + "\"surname\": \"Chambers\",\n" + "\"fullname\": \"Suzanne Underwood\",\n"
        + "\"email\": \"joyce@richmond.gw\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 65,\n"
        + "\"index_start_at\": 120,\n" + "\"integer\": 38,\n" + "\"float\": 16.0153,\n" + "\"name\": \"Vickie\",\n"
        + "\"surname\": \"Underwood\",\n" + "\"fullname\": \"Neal Tyler\",\n" + "\"email\": \"sandy@matthews.ie\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 66,\n" + "\"index_start_at\": 121,\n"
        + "\"integer\": 43,\n" + "\"float\": 18.8678,\n" + "\"name\": \"Lorraine\",\n" + "\"surname\": \"Britt\",\n"
        + "\"fullname\": \"Suzanne Henry\",\n" + "\"email\": \"kenneth@wallace.be\",\n" + "\"bool\": true\n" + "},\n"
        + "{\n" + "\"index\": 67,\n" + "\"index_start_at\": 122,\n" + "\"integer\": 48,\n" + "\"float\": 14.9018,\n"
        + "\"name\": \"Keith\",\n" + "\"surname\": \"Copeland\",\n" + "\"fullname\": \"Sidney Creech\",\n"
        + "\"email\": \"lynne@parrott.gp\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 68,\n"
        + "\"index_start_at\": 123,\n" + "\"integer\": 10,\n" + "\"float\": 19.8478,\n" + "\"name\": \"Stacy\",\n"
        + "\"surname\": \"Rosenthal\",\n" + "\"fullname\": \"Stephen Harrell\",\n" + "\"email\": \"claire@case.lv\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 69,\n" + "\"index_start_at\": 124,\n"
        + "\"integer\": 23,\n" + "\"float\": 19.7499,\n" + "\"name\": \"Jordan\",\n" + "\"surname\": \"Hood\",\n"
        + "\"fullname\": \"Anita Steele\",\n" + "\"email\": \"chris@weiss.dj\",\n" + "\"bool\": true\n" + "},\n" + "{\n"
        + "\"index\": 70,\n" + "\"index_start_at\": 125,\n" + "\"integer\": 7,\n" + "\"float\": 10.7895,\n"
        + "\"name\": \"Martin\",\n" + "\"surname\": \"Dolan\",\n" + "\"fullname\": \"Maria McMahon\",\n"
        + "\"email\": \"barbara@moss.sn\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 71,\n"
        + "\"index_start_at\": 126,\n" + "\"integer\": 25,\n" + "\"float\": 18.6945,\n" + "\"name\": \"Diana\",\n"
        + "\"surname\": \"Kent\",\n" + "\"fullname\": \"Joe Norman\",\n" + "\"email\": \"kay@sherrill.lr\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 72,\n" + "\"index_start_at\": 127,\n"
        + "\"integer\": 25,\n" + "\"float\": 12.9575,\n" + "\"name\": \"Ethel\",\n" + "\"surname\": \"Parrott\",\n"
        + "\"fullname\": \"Diana Norman\",\n" + "\"email\": \"bob@bender.no\",\n" + "\"bool\": false\n" + "},\n" + "{\n"
        + "\"index\": 73,\n" + "\"index_start_at\": 128,\n" + "\"integer\": 45,\n" + "\"float\": 17.0633,\n"
        + "\"name\": \"Lewis\",\n" + "\"surname\": \"McNeill\",\n" + "\"fullname\": \"Paula Sharp\",\n"
        + "\"email\": \"jacob@kennedy.nr\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 74,\n"
        + "\"index_start_at\": 129,\n" + "\"integer\": 13,\n" + "\"float\": 17.004,\n" + "\"name\": \"Justin\",\n"
        + "\"surname\": \"Wu\",\n" + "\"fullname\": \"Clifford Cherry\",\n" + "\"email\": \"warren@puckett.mz\",\n"
        + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 75,\n" + "\"index_start_at\": 130,\n" + "\"integer\": 24,\n"
        + "\"float\": 15.3502,\n" + "\"name\": \"Nina\",\n" + "\"surname\": \"Morgan\",\n"
        + "\"fullname\": \"Steve Alston\",\n" + "\"email\": \"joseph@mcallister.uy\",\n" + "\"bool\": true\n" + "},\n"
        + "{\n" + "\"index\": 76,\n" + "\"index_start_at\": 131,\n" + "\"integer\": 13,\n" + "\"float\": 12.9034,\n"
        + "\"name\": \"Lillian\",\n" + "\"surname\": \"Farmer\",\n" + "\"fullname\": \"Bob Brown\",\n"
        + "\"email\": \"don@creech.th\",\n" + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 77,\n"
        + "\"index_start_at\": 132,\n" + "\"integer\": 10,\n" + "\"float\": 17.1633,\n" + "\"name\": \"Kimberly\",\n"
        + "\"surname\": \"Rich\",\n" + "\"fullname\": \"Sandra Rich\",\n" + "\"email\": \"bob@matthews.gp\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 78,\n" + "\"index_start_at\": 133,\n"
        + "\"integer\": 31,\n" + "\"float\": 15.4871,\n" + "\"name\": \"Nancy\",\n" + "\"surname\": \"Harrell\",\n"
        + "\"fullname\": \"Katharine Rodgers\",\n" + "\"email\": \"jeff@boyd.ir\",\n" + "\"bool\": true\n" + "},\n"
        + "{\n" + "\"index\": 79,\n" + "\"index_start_at\": 134,\n" + "\"integer\": 47,\n" + "\"float\": 15.2667,\n"
        + "\"name\": \"Kent\",\n" + "\"surname\": \"McNamara\",\n" + "\"fullname\": \"Glen Bowles\",\n"
        + "\"email\": \"dolores@boyd.ua\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 80,\n"
        + "\"index_start_at\": 135,\n" + "\"integer\": 33,\n" + "\"float\": 10.1659,\n" + "\"name\": \"Gayle\",\n"
        + "\"surname\": \"Harris\",\n" + "\"fullname\": \"Greg Horowitz\",\n" + "\"email\": \"ronnie@pritchard.vn\",\n"
        + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 81,\n" + "\"index_start_at\": 136,\n" + "\"integer\": 47,\n"
        + "\"float\": 19.9769,\n" + "\"name\": \"Sheryl\",\n" + "\"surname\": \"Brock\",\n"
        + "\"fullname\": \"Becky Cates\",\n" + "\"email\": \"calvin@lyons.pf\",\n" + "\"bool\": false\n" + "},\n"
        + "{\n" + "\"index\": 82,\n" + "\"index_start_at\": 137,\n" + "\"integer\": 31,\n" + "\"float\": 16.3667,\n"
        + "\"name\": \"Benjamin\",\n" + "\"surname\": \"Ritchie\",\n" + "\"fullname\": \"Beth McKenna\",\n"
        + "\"email\": \"michelle@wolf.ee\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 83,\n"
        + "\"index_start_at\": 138,\n" + "\"integer\": 19,\n" + "\"float\": 15.6796,\n" + "\"name\": \"Roy\",\n"
        + "\"surname\": \"Hartman\",\n" + "\"fullname\": \"Mike Barrett\",\n" + "\"email\": \"joanne@barefoot.td\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 84,\n" + "\"index_start_at\": 139,\n"
        + "\"integer\": 17,\n" + "\"float\": 14.1217,\n" + "\"name\": \"Walter\",\n" + "\"surname\": \"Fox\",\n"
        + "\"fullname\": \"Billy Fischer\",\n" + "\"email\": \"vincent@aldridge.im\",\n" + "\"bool\": true\n" + "},\n"
        + "{\n" + "\"index\": 85,\n" + "\"index_start_at\": 140,\n" + "\"integer\": 45,\n" + "\"float\": 17.6468,\n"
        + "\"name\": \"Lillian\",\n" + "\"surname\": \"Chung\",\n" + "\"fullname\": \"Annie Duke\",\n"
        + "\"email\": \"diana@crabtree.tw\",\n" + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 86,\n"
        + "\"index_start_at\": 141,\n" + "\"integer\": 38,\n" + "\"float\": 12.2853,\n" + "\"name\": \"Brett\",\n"
        + "\"surname\": \"Moran\",\n" + "\"fullname\": \"Robert Hensley\",\n" + "\"email\": \"steve@reid.st\",\n"
        + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 87,\n" + "\"index_start_at\": 142,\n" + "\"integer\": 34,\n"
        + "\"float\": 10.7395,\n" + "\"name\": \"Caroline\",\n" + "\"surname\": \"Boyette\",\n"
        + "\"fullname\": \"Edwin Aycock\",\n" + "\"email\": \"lester@byrne.bi\",\n" + "\"bool\": true\n" + "},\n"
        + "{\n" + "\"index\": 88,\n" + "\"index_start_at\": 143,\n" + "\"integer\": 28,\n" + "\"float\": 18.9139,\n"
        + "\"name\": \"Paige\",\n" + "\"surname\": \"Waller\",\n" + "\"fullname\": \"Shannon Atkins\",\n"
        + "\"email\": \"gina@vaughn.lk\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 89,\n"
        + "\"index_start_at\": 144,\n" + "\"integer\": 33,\n" + "\"float\": 14.1008,\n" + "\"name\": \"Allan\",\n"
        + "\"surname\": \"Clements\",\n" + "\"fullname\": \"Gene Horn\",\n" + "\"email\": \"lloyd@nichols.bm\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 90,\n" + "\"index_start_at\": 145,\n"
        + "\"integer\": 33,\n" + "\"float\": 10.4631,\n" + "\"name\": \"Christine\",\n" + "\"surname\": \"Berry\",\n"
        + "\"fullname\": \"Toni Denton\",\n" + "\"email\": \"paula@koch.tn\",\n" + "\"bool\": true\n" + "},\n" + "{\n"
        + "\"index\": 91,\n" + "\"index_start_at\": 146,\n" + "\"integer\": 8,\n" + "\"float\": 17.5477,\n"
        + "\"name\": \"Paula\",\n" + "\"surname\": \"Duke\",\n" + "\"fullname\": \"Kathy Davidson\",\n"
        + "\"email\": \"joan@goldman.pw\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 92,\n"
        + "\"index_start_at\": 147,\n" + "\"integer\": 41,\n" + "\"float\": 14.67,\n" + "\"name\": \"Gloria\",\n"
        + "\"surname\": \"Jensen\",\n" + "\"fullname\": \"Tommy Farmer\",\n" + "\"email\": \"neal@diaz.tn\",\n"
        + "\"bool\": true\n" + "},\n" + "{\n" + "\"index\": 93,\n" + "\"index_start_at\": 148,\n" + "\"integer\": 15,\n"
        + "\"float\": 16.226,\n" + "\"name\": \"Maria\",\n" + "\"surname\": \"Coble\",\n"
        + "\"fullname\": \"Sue Cooper\",\n" + "\"email\": \"martha@richardson.lk\",\n" + "\"bool\": false\n" + "},\n"
        + "{\n" + "\"index\": 94,\n" + "\"index_start_at\": 149,\n" + "\"integer\": 20,\n" + "\"float\": 16.571,\n"
        + "\"name\": \"Marian\",\n" + "\"surname\": \"Dunlap\",\n" + "\"fullname\": \"Andrew Strickland\",\n"
        + "\"email\": \"frederick@singh.lu\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 95,\n"
        + "\"index_start_at\": 150,\n" + "\"integer\": 39,\n" + "\"float\": 18.4427,\n" + "\"name\": \"Kathy\",\n"
        + "\"surname\": \"Nash\",\n" + "\"fullname\": \"Harold Currie\",\n" + "\"email\": \"gretchen@long.sk\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 96,\n" + "\"index_start_at\": 151,\n"
        + "\"integer\": 49,\n" + "\"float\": 11.9224,\n" + "\"name\": \"Gretchen\",\n" + "\"surname\": \"Bunn\",\n"
        + "\"fullname\": \"Ruth Riley\",\n" + "\"email\": \"frederick@hughes.sl\",\n" + "\"bool\": false\n" + "},\n"
        + "{\n" + "\"index\": 97,\n" + "\"index_start_at\": 152,\n" + "\"integer\": 44,\n" + "\"float\": 16.7709,\n"
        + "\"name\": \"Harvey\",\n" + "\"surname\": \"Cox\",\n" + "\"fullname\": \"Greg Fisher\",\n"
        + "\"email\": \"jose@murphy.tv\",\n" + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 98,\n"
        + "\"index_start_at\": 153,\n" + "\"integer\": 48,\n" + "\"float\": 13.4941,\n" + "\"name\": \"Robert\",\n"
        + "\"surname\": \"Waller\",\n" + "\"fullname\": \"Alice McDonald\",\n" + "\"email\": \"beth@turner.lu\",\n"
        + "\"bool\": false\n" + "},\n" + "{\n" + "\"index\": 99,\n" + "\"index_start_at\": 154,\n" + "\"integer\": 5,\n"
        + "\"float\": 12.3306,\n" + "\"name\": \"Ronnie\",\n" + "\"surname\": \"Savage\",\n"
        + "\"fullname\": \"Bill Wiggins\",\n" + "\"email\": \"nicholas@blum.nf\",\n" + "\"bool\": false\n" + "},\n"
        + "{\n" + "\"index\": 100,\n" + "\"index_start_at\": 155,\n" + "\"integer\": 5,\n" + "\"float\": 11.3392,\n"
        + "\"name\": \"Christy\",\n" + "\"surname\": \"Reilly\",\n" + "\"fullname\": \"Lynn Livingston\",\n"
        + "\"email\": \"gloria@walton.ao\",\n" + "\"bool\": true\n" + "}\n" + "]\n" + "}";
  }

  private static String getStringData() {
    StringBuilder stringBuilder = new StringBuilder("\"");
    for (int i = 0; i < 1000; i++) {
      stringBuilder.append(UUID.randomUUID());
    }
    return stringBuilder.append("\"").toString();
  }
}
