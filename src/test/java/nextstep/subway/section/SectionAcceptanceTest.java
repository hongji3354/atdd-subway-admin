package nextstep.subway.section;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.domain.line.LineAcceptanceTest;
import nextstep.subway.domain.line.dto.LineResponse;
import nextstep.subway.domain.line.dto.SectionRequest;
import nextstep.subway.domain.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 구간 관련 기능")
public class SectionAcceptanceTest extends AcceptanceTest {

    private final int SECTION_DISTANCE = 100;
    private StationResponse upStation;
    private StationResponse downStation;
    private StationResponse firstAddStation;
    private StationResponse secondAddStation;
    private LineResponse lineResponse;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        final LineAcceptanceTest lineAcceptanceTest = new LineAcceptanceTest();
        upStation = lineAcceptanceTest.종점역_생성("소요산");
        downStation = lineAcceptanceTest.종점역_생성("인천");
        firstAddStation = lineAcceptanceTest.추가역_생성("의정부");
        secondAddStation = lineAcceptanceTest.추가역_생성("서울역");
        lineResponse = lineAcceptanceTest.종점역_정보를_포함한_지하철_노선_생성(upStation, downStation, "5호선", "purple", SECTION_DISTANCE)
                .as(LineResponse.class);
    }

    @DisplayName("역 사이에 새로운 역을 추가한다.")
    @Test
    void addNewStationBetweenStation() {
        //when
        final ExtractableResponse<Response> response = 새로운_역_추가(lineResponse.getId(), upStation.getId(), firstAddStation.getId(), 10);

        //then
        //역_사이에_새로운_역_추가됨
        final LineResponse lineResponse = response.as(LineResponse.class);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(lineResponse.getStations()).extracting("name").containsExactly("소요산", "의정부", "인천");
    }

    @DisplayName("새로운 역을 상행 종점으로 추가한다.")
    @Test
    void addNewStationAscendingLastStopStation() {
        //when
        final ExtractableResponse<Response> response = 새로운_역_추가(lineResponse.getId(), firstAddStation.getId(), upStation.getId(),10);

        //then
        //새로운_역_상행_종점_으로_추가됨
        final LineResponse lineResponse = response.as(LineResponse.class);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(lineResponse.getStations()).extracting("name").containsExactly("의정부", "소요산", "인천");
    }

    @DisplayName("새로운 역을 하행 종점으로 추가한다.")
    @Test
    void addNewStationDescendingLastStopStation() {
        //when
        final ExtractableResponse<Response> response = 새로운_역_추가(lineResponse.getId(), downStation.getId(), firstAddStation.getId(), 10);

        //then
        //새로운_역_하행_종점_으로_추가됨
        final LineResponse lineResponse = response.as(LineResponse.class);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(lineResponse.getStations()).extracting("name").containsExactly("소요산", "인천", "의정부");
    }

    @DisplayName("역 사이에 새로운 역을 추가시 기존 역 사이보다 크거나 같으면 추가할 수 없다.")
    @Test
    void addNewStationBetweenStationFailure() {
        //when
        final ExtractableResponse<Response> response = 새로운_역_추가(lineResponse.getId(), upStation.getId(), firstAddStation.getId(), SECTION_DISTANCE);

        //then
        //역_사이에_새로운_역_추가_실패
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("상행역과 하행역이 이미 노선에 모두 등록되어 있다면 추가할 수 없다.")
    @Test
    void alreadyRegisterSection() {
        //when
        final ExtractableResponse<Response> response = 새로운_역_추가(lineResponse.getId(), upStation.getId(), downStation.getId(), 10);

        //then
        //새로운_역_하행_종점_으로_추가_실패
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("상행역과 하행역 둘 중 하나도 포함되어있지 않으면 추가할 수 없다.")
    @Test
    void standardStationNotFound() {
        //when
        final ExtractableResponse<Response> response = 새로운_역_추가(lineResponse.getId(), firstAddStation.getId(), secondAddStation.getId(), 10);

        //then
        //새로운_역_하행_종점_으로_추가_실패
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private ExtractableResponse<Response> 새로운_역_추가(Long lineId, Long upStationId, Long downStationId, int distance) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new SectionRequest(upStationId, downStationId, distance))
                .pathParam("lineId", lineId)
                .when().post("/lines/{lineId}/sections")
                .then().log().all().extract();
    }
}


