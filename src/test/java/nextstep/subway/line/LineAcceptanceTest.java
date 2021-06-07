package nextstep.subway.line;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.dto.StationResponse;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

    private StationResponse 강남역;
    private StationResponse 역삼역;
    private StationResponse 서울역;
    private StationResponse 시청역;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        강남역 = StationAcceptanceTest.지하철_역_등록되어_있음_공용("강남역");
        역삼역 = StationAcceptanceTest.지하철_역_등록되어_있음_공용("역삼역");
        서울역 = StationAcceptanceTest.지하철_역_등록되어_있음_공용("서울역");
        시청역 = StationAcceptanceTest.지하철_역_등록되어_있음_공용("시청역");
    }

    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLine() {
        // when
        ExtractableResponse<Response> response = 지하철_노선_생성_요청(new LineRequest("1호선", "blue", 강남역.getId(), 역삼역.getId(), 10));

        // then
        지하철_노선_생성됨(response);
    }

    @DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성한다.")
    @Test
    void createLine2() {
        // given
        LineRequest greenLineRequest = new LineRequest("2호선", "green", 강남역.getId(), 역삼역.getId(), 5);
        지하철_노선_등록되어_있음(greenLineRequest);

        // when
        ExtractableResponse<Response> response = 지하철_노선_생성_요청(greenLineRequest);

        // then
        지하철_노선_생성_실패됨(response);
        지하철_요청_실패_메시지_확인됨(response, "노선 생성에 실패했습니다. 이미 존재하는 노선입니다.");
    }

    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void getLines() {
        // given
        LineResponse greenLineResponse = 지하철_노선_등록되어_있음(new LineRequest("2호선", "green", 강남역.getId(), 역삼역.getId(), 5));
        LineResponse blueLineResponse = 지하철_노선_등록되어_있음(new LineRequest("4호선", "blue", 서울역.getId(), 시청역.getId(), 9));

        // when
        ExtractableResponse<Response> response = 지하철_노선_목록_조회_요청();

        // then
        지하철_노선_목록_응답됨(response);
        List<Long> lineIds = Arrays.asList(blueLineResponse.getId(), greenLineResponse.getId());
        지하철_노선_목록_포함됨(response, lineIds);
    }

    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void getLine() {
        // given
        LineResponse blueLineResponse = 지하철_노선_등록되어_있음(new LineRequest("2호선", "green", 강남역.getId(), 역삼역.getId(), 10));

        // when
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(blueLineResponse.getId());

        // then
        지하철_노선_응답됨(response);
        지하철_노선_역_포함됨(response, Arrays.asList(강남역, 역삼역));
    }

    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLine() {
        // given
        LineResponse greenLineResponse = 지하철_노선_등록되어_있음(new LineRequest("2호선", "green", 강남역.getId(), 역삼역.getId(), 5));

        // when
        ExtractableResponse<Response> response = 지하철_노선_수정_요청(greenLineResponse.getId(),
                new LineRequest("4호선", "blue", 강남역.getId(), 역삼역.getId(), 5));

        // then
        지하철_노선_수정됨(response);
    }

    @DisplayName("지하철 노선을 제거한다.")
    @Test
    void deleteLine() {
        // given
        LineResponse greenLineResponse = 지하철_노선_등록되어_있음(new LineRequest("2호선", "green", 강남역.getId(), 역삼역.getId(), 5));

        // when
        ExtractableResponse<Response> response = 지하철_노선_제거_요청(greenLineResponse.getId());

        // then
        지하철_노선_삭제됨(response);
    }

    @DisplayName("등록되지 않은 노선 조회 실패")
    @Test
    void getLine_error() {
        // given
        LineResponse greenLineResponse = 지하철_노선_등록되어_있음(new LineRequest("2호선", "green", 강남역.getId(), 역삼역.getId(), 5));

        // when
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(greenLineResponse.getId() + 1L);

        // then
        지하철_노선_응답_실패됨(response);
        지하철_요청_실패_메시지_확인됨(response, "노선이 존재하지 않습니다.");
    }

    @DisplayName("등록되지 않은 노선 수정 시도시 실패")
    @Test
    void updateLine_error() {
        // given
        LineResponse greenLineResponse = 지하철_노선_등록되어_있음(new LineRequest("2호선", "green", 강남역.getId(), 역삼역.getId(), 5));

        // when
        ExtractableResponse<Response> response = 지하철_노선_수정_요청(greenLineResponse.getId() + 1L, new LineRequest("4호선", "blue", 강남역.getId(), 역삼역.getId(), 5));

        // then
        지하철_노선_수정_실패됨(response);
        지하철_요청_실패_메시지_확인됨(response, "수정 대상 노선이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("동일한 노선이름으로 수정 요청할 경우 실패")
    void duplicate_name_error() {
        // given
        지하철_노선_등록되어_있음(new LineRequest("2호선", "green", 강남역.getId(), 역삼역.getId(), 5));
        LineResponse blueLineResponse = 지하철_노선_등록되어_있음(new LineRequest("4호선", "blue", 서울역.getId(), 시청역.getId(), 9));

        // when
        ExtractableResponse<Response> response = 지하철_노선_수정_요청(blueLineResponse.getId(),
                new LineRequest("2호선", "green"));

        // then
        지하철_노선_수정_실패됨(response);
        지하철_요청_실패_메시지_확인됨(response, "동일한 이름의 노선이 존재합니다.");
    }

    @DisplayName("등록되지 않은 노선 삭제 시도시 실패")
    @Test
    void deleteLine_error() {
        // given
        LineResponse greenLineResponse = 지하철_노선_등록되어_있음(new LineRequest("2호선", "green", 강남역.getId(), 역삼역.getId(), 5));

        // when
        ExtractableResponse<Response> response = 지하철_노선_제거_요청(greenLineResponse.getId() + 1L);

        // then
        지하철_노선_삭제_실패됨(response);
        지하철_요청_실패_메시지_확인됨(response, "삭제 대상 노선이 존재하지 않습니다.");
    }

    public static LineResponse 지하철_노선_등록되어_있음_공용(LineRequest lineRequest) {
        return RestAssured.given().log().all()
                .body(lineRequest)
                .accept(MediaType.ALL_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/lines")
                .then().log().all()
                .extract()
                .jsonPath()
                .getObject(".", LineResponse.class);
    }

    private ExtractableResponse<Response> 지하철_노선_생성_요청(LineRequest lineRequest) {
        return RestAssured.given().log().all()
                .body(lineRequest)
                .accept(MediaType.ALL_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/lines")
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> 지하철_노선_목록_조회_요청() {
        return RestAssured.given().log().all()
                .when()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/lines")
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> 지하철_노선_조회_요청(Long id) {
        return RestAssured.given().log().all()
                .when()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/lines/" + id)
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> 지하철_노선_수정_요청(Long id, LineRequest lineRequest) {
        return RestAssured.given().log().all()
                .body(lineRequest)
                .accept(MediaType.ALL_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .put("/lines/" + id)
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> 지하철_노선_제거_요청(Long id) {
        return RestAssured.given().log().all()
                .when()
                .accept(MediaType.ALL_VALUE)
                .delete("/lines/" + id)
                .then().log().all()
                .extract();
    }

    private void 지하철_노선_목록_포함됨(ExtractableResponse<Response> response, List<Long> lineIds) {
        List<Long> responseIds = response.jsonPath().getList("id", Long.class);
        assertThat(responseIds).containsAll(lineIds);
    }

    private LineResponse 지하철_노선_등록되어_있음(LineRequest lineRequest) {
        return 지하철_노선_생성_요청(lineRequest)
                .jsonPath()
                .getObject(".", LineResponse.class);
    }

    private void 지하철_노선_생성됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    private void 지하철_노선_생성_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 지하철_노선_목록_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 지하철_노선_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 지하철_노선_수정됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 지하철_노선_삭제됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    private void 지하철_노선_응답_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 지하철_노선_수정_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 지하철_노선_삭제_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 지하철_요청_실패_메시지_확인됨(ExtractableResponse<Response> response, String userErrorMessage) {
        String errorMessage = response.jsonPath().getObject("errorMessage", String.class);
        assertThat(errorMessage).isEqualTo(userErrorMessage);
    }

    private void 지하철_노선_역_포함됨(ExtractableResponse<Response> response, List<StationResponse> stationResponses) {
        List<StationResponse> resultStationResponses = response.jsonPath().getList("stations", StationResponse.class);
        assertThat(resultStationResponses).containsAll(stationResponses);
    }
}
