package nextstep.subway.section;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.LineAcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.section.dto.SectionRequest;
import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.dto.StationResponse;

@DisplayName("지하철 구간 관련 기능")
public class SectionAcceptanceTest extends AcceptanceTest {

    StationResponse 강남역;
    StationResponse 교대역;
    StationResponse 서초역;
    StationResponse 방배역;
    StationResponse 사당역;
    StationResponse 낙성대역;
    StationResponse 서울대입구역;
    StationResponse 봉천역;
    StationResponse 신림역;
    StationResponse 신대방역;
    StationResponse 대림역;
    LineResponse greenLineResponse;

    @BeforeEach
    public void setUp() {
        super.setUp();
        List<String> stations = Arrays.asList("강남역", "교대역", "서초역", "방배역", "사당역", "낙성대역", "서울대입구역", "봉천역",
                "신림역", "신대방역", "대림역");
        Map<String, StationResponse> responses = StationAcceptanceTest.지하철_역들이_등록되어_있음_공용(stations).stream()
                .collect(Collectors.toMap(res -> res.getName(), res -> res));
        강남역 = responses.get("강남역");
        교대역 = responses.get("교대역");
        서초역 = responses.get("서초역");
        방배역 = responses.get("방배역");
        사당역 = responses.get("사당역");
        낙성대역 = responses.get("낙성대역");
        서울대입구역 = responses.get("서울대입구역");
        봉천역 = responses.get("봉천역");
        신림역 = responses.get("신림역");
        신대방역 = responses.get("신대방역");
        대림역 = responses.get("대림역");
        LineRequest lineRequest = new LineRequest("2호선", "green", 강남역.getId(), 교대역.getId(), 4);
        greenLineResponse = LineAcceptanceTest.지하철_노선_등록되어_있음_공용(lineRequest);
        지하철_구간_생성되어_있음(greenLineResponse, 교대역, 서초역, 3);
        지하철_구간_생성되어_있음(greenLineResponse, 서초역, 사당역, 3);
        지하철_구간_생성되어_있음(greenLineResponse, 사당역, 낙성대역, 3);
    }

    @Test
    @DisplayName("신규 생성 노선에 구간 추가등록")
    void create() {
        // when
        ExtractableResponse<Response> response = 지하철_구간_생성_요청(greenLineResponse.getId(),
                new SectionRequest(낙성대역.getId(), 서울대입구역.getId(), 3));

        // then
        지하철_구간_생성됨(response);
    }

    @Test
    @DisplayName("신규 구간을 상행역 기준으로 기존 구간 중간에 생성한다.")
    void insertSection_to_middle1() {
        // when
        ExtractableResponse<Response> response = 지하철_구간_생성_요청(greenLineResponse.getId(),
                new SectionRequest(교대역.getId(), 방배역.getId(), 4));

        // then
        지하철_중간구간_추가됨(response);
        지하철_노선_역_순서_확인됨(response, Arrays.asList(강남역, 교대역, 서초역, 방배역, 사당역, 낙성대역));
    }

    @Test
    @DisplayName("신규 구간을 하행역 기준으로 기존 구간 중간에 생성한다.")
    void insertSection_to_middle2() {
        // when
        ExtractableResponse<Response> response = 지하철_구간_생성_요청(greenLineResponse.getId(),
                new SectionRequest(방배역.getId(), 낙성대역.getId(), 4));

        // then
        지하철_중간구간_추가됨(response);
        지하철_노선_역_순서_확인됨(response, Arrays.asList(강남역, 교대역, 서초역, 방배역, 사당역, 낙성대역));
    }

    @Test
    @DisplayName("신규 구간을 기존 구간 가장 앞에 생성한다.")
    void insertSection_to_front() {
        // when
        ExtractableResponse<Response> response = 지하철_구간_생성_요청(greenLineResponse.getId(),
                new SectionRequest(낙성대역.getId(), 신림역.getId(), 4));

        // then
        지하철_선두구간_추가됨(response);
        지하철_노선_역_순서_확인됨(response, Arrays.asList(강남역, 교대역, 서초역, 사당역, 낙성대역, 신림역));
    }

    @Test
    @DisplayName("신규 구간을 기존 구간 가장 뒤에 생성한다.")
    void insertSection_at_back() {
        // when
        ExtractableResponse<Response> response = 지하철_구간_생성_요청(greenLineResponse.getId(),
                new SectionRequest(신림역.getId(), 강남역.getId(), 4));

        // then
        지하철_후발구간_추가됨(response);
        지하철_노선_역_순서_확인됨(response, Arrays.asList(신림역, 강남역, 교대역, 서초역, 사당역, 낙성대역));
    }

    @Test
    @DisplayName("등록하려는 두개의 역이 이미 존재할 경우 예외발생")
    void duplicate_exception1() {
        // when
        ExtractableResponse<Response> response = 지하철_구간_생성_요청(greenLineResponse.getId(),
                new SectionRequest(교대역.getId(), 사당역.getId(), 4));

        // then
        지하철_구간에_동록된_상하행역_추가등록_실패됨(response);
        지하철_구간_요청_실패_메시지_확인됨(response, "상행역, 하행역이 이미 존재합니다.");
    }

    @Test
    @DisplayName("등록하려는 두개의 역이 반대로 이미 존재할 경우 예외발생")
    void duplicate_exception2() {
        // when
        ExtractableResponse<Response> response = 지하철_구간_생성_요청(greenLineResponse.getId(),
                new SectionRequest(사당역.getId(), 교대역.getId(), 4));

        // then
        지하철_구간에_동록된_상하행역_추가등록_실패됨(response);
        지하철_구간_요청_실패_메시지_확인됨(response, "상행역, 하행역이 이미 존재합니다.");
    }

    @Test
    @DisplayName("상행역 기준으로 하행역과 동일한 거리에 이미 역이 존재할 경우 예외발생")
    void same_distance_exception1() {
        // when
        ExtractableResponse<Response> response = 지하철_구간_생성_요청(greenLineResponse.getId(),
                new SectionRequest(교대역.getId(), 신림역.getId(), 3));

        // then
        상행역기준_하행역_위치거리에_역이_존재할_경우_추가등록_실패(response);
        지하철_구간_요청_실패_메시지_확인됨(response, "같은 길이의 구간이 존재합니다.");
    }

    @Test
    @DisplayName("하행역 기준으로 상행역과 동일한 거리에 이미 역이 존재할 경우 예외발생")
    void same_distance_exception2() {
        // when
        ExtractableResponse<Response> response = 지하철_구간_생성_요청(greenLineResponse.getId(),
                new SectionRequest(신림역.getId(), 사당역.getId(), 6));

        // then
        하행역기준_상행역_위치거리에_역이_존재할_경우_추가등록_실패(response);
        지하철_구간_요청_실패_메시지_확인됨(response, "같은 길이의 구간이 존재합니다.");
    }

    @Test
    @DisplayName("등록하려는 구간에 포함된 두 역이 기존 구간에 등록되지 않은 역일 경우 예외처리")
    void notIncluding_stations() {
        // when
        ExtractableResponse<Response> response = 지하철_구간_생성_요청(greenLineResponse.getId(),
                new SectionRequest(신림역.getId(), 봉천역.getId(), 6));

        // then
        기존_구간에_등록되지_않은_역들_추가등록_실패(response);
        지하철_구간_요청_실패_메시지_확인됨(response, "상,하행역 모두 기존 노선에 포함된 역이 아닙니다.");
    }

    @Test
    @DisplayName("지하철 구간 삭제 요청")
    void remove_section() {
        // when
        ExtractableResponse<Response> response = 지하철_구간_삭제_요청(greenLineResponse.getId(), 서초역.getId());

        // then
        지하철_구간_삭제됨(response);
        조회된_노선에서_삭제된_역_포함되지_않음_확인(greenLineResponse.getId(), 서초역);
    }

    @Test
    @DisplayName("등록되지 않은 노선으로 구간 삭제 요청 시 예외처리")
    void remove_section_exception1() {
        // when
        ExtractableResponse<Response> response = 지하철_구간_삭제_요청(10L, 서초역.getId());

        // then
        등록되지_않은_지하철_구간_삭제요청_실패함(response);
        지하철_구간_요청_실패_메시지_확인됨(response, "노선이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("등록되지 않은 역 삭제 요청 시 예외처리")
    void remove_section_by_not_exist_station_exception() {
        // when
        ExtractableResponse<Response> response = 지하철_구간_삭제_요청(greenLineResponse.getId(), 1000L);

        // then
        등록되지_않은_지하철_역_삭제요청_실패함(response);
        지하철_구간_요청_실패_메시지_확인됨(response, "등록된 역이 아닙니다. 역 ID : 1000");
    }

    @Test
    @DisplayName("노선에 포함되지 않은 역 삭제 요청 시 예외처리")
    void remove_section_by_notIncludingStation_exception() {
        // when
        ExtractableResponse<Response> response = 지하철_구간_삭제_요청(greenLineResponse.getId(), 대림역.getId());

        // then
        노선구간에_포함되지_않은_지하철_역_삭제요청_실패함(response);
        지하철_구간_요청_실패_메시지_확인됨(response, "구간에 포함된 역이 아닙니다.");
    }

    @Test
    @DisplayName("마지막 하나의 구간만 남아있을 경우 구간에 포함 된 역 삭제 요청 시 예외처리")
    void remove_section_by_lastSection_exception() {
        // given
        지하철_구간_삭제되어_있음(greenLineResponse.getId(), 강남역.getId());
        지하철_구간_삭제되어_있음(greenLineResponse.getId(), 교대역.getId());
        지하철_구간_삭제되어_있음(greenLineResponse.getId(), 서초역.getId());

        // when
        ExtractableResponse<Response> response = 지하철_구간_삭제_요청(greenLineResponse.getId(), 사당역.getId());

        // then
        마지막_남은_구간에_포함된_지하철_역_삭제요청_실패함(response);
        지하철_구간_요청_실패_메시지_확인됨(response, "마지막 구간에 포함된 역은 삭제할 수 없습니다.");
    }

    private void 지하철_구간_삭제되어_있음(Long lineId, Long stationId) {
        RestAssured.given().log().all()
                .when()
                .accept(MediaType.ALL_VALUE)
                .delete("/lines/" + lineId + "/sections?stationId=" + stationId)
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> 지하철_구간_삭제_요청(Long lineId, Long stationId) {
        return RestAssured.given().log().all()
                .when()
                .accept(MediaType.ALL_VALUE)
                .delete("/lines/" + lineId + "/sections?stationId=" + stationId)
                .then().log().all()
                .extract();
    }

    private LineResponse 지하철_구간_생성되어_있음(LineResponse greenLineResponse, StationResponse upStationResponse,
                                        StationResponse downStationResponse, int distance) {
        return 지하철_구간_생성_요청(greenLineResponse.getId(), new SectionRequest(upStationResponse.getId(),
                downStationResponse.getId(), distance))
                .jsonPath()
                .getObject(".", LineResponse.class);
    }

    private ExtractableResponse<Response> 지하철_구간_생성_요청(Long lineId, SectionRequest sectionRequest) {
        return RestAssured.given().log().all()
                .when()
                .body(sectionRequest)
                .accept(MediaType.ALL_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .post("/lines/" + lineId + "/sections")
                .then().log().all()
                .extract();
    }

    private void 지하철_노선_역_순서_확인됨(ExtractableResponse<Response> response, List<StationResponse> targetStations) {
        List<StationResponse> findStations = response.jsonPath().getList("stations", StationResponse.class);
        assertThat(findStations.containsAll(targetStations)).isTrue();
    }

    private void 조회된_노선에서_삭제된_역_포함되지_않음_확인(Long lineId, StationResponse 서초역) {
        List<String> resultStationNames = LineAcceptanceTest.지하철_노선_조회_요청_공용(lineId)
                .jsonPath()
                .getList("stations", StationResponse.class)
                .stream()
                .map(StationResponse::getName)
                .collect(Collectors.toList());
        assertThat(resultStationNames.contains(서초역.getName())).isFalse();
    }

    private void 지하철_구간_생성됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    private void 지하철_중간구간_추가됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    private void 지하철_선두구간_추가됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    private void 지하철_후발구간_추가됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    private void 지하철_구간_요청_실패_메시지_확인됨(ExtractableResponse<Response> response, String userErrorMessage) {
        String errorMessage = response.jsonPath().getObject("errorMessage", String.class);
        assertThat(errorMessage).isEqualTo(userErrorMessage);
    }

    private void 지하철_구간에_동록된_상하행역_추가등록_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 상행역기준_하행역_위치거리에_역이_존재할_경우_추가등록_실패(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 하행역기준_상행역_위치거리에_역이_존재할_경우_추가등록_실패(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 기존_구간에_등록되지_않은_역들_추가등록_실패(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 지하철_구간_삭제됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 등록되지_않은_지하철_구간_삭제요청_실패함(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 등록되지_않은_지하철_역_삭제요청_실패함(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 노선구간에_포함되지_않은_지하철_역_삭제요청_실패함(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 마지막_남은_구간에_포함된_지하철_역_삭제요청_실패함(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
