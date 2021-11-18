package nextstep.subway.line;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLine() {
        // when
        // 지하철_노선_생성_요청
        final ExtractableResponse<Response> response = 지하철_노선_생성("2호선", "green");

        // then
        // 지하철_노선_생성됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성한다.")
    @Test
    void createLine2() {
        // given
        // 지하철_노선_등록되어_있음
        String lineName = "2호선";
        지하철_노선_생성(lineName,"green");

        // when
        // 지하철_노선_생성_요청
        final ExtractableResponse<Response> response = 지하철_노선_생성(lineName, "red");

        // then
        // 지하철_노선_생성_실패됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void getLines() {
        // given
        // 지하철_노선_등록되어_있음
        // 지하철_노선_등록되어_있음
        지하철_노선_생성("1호선","blue");
        지하철_노선_생성("2호선","green");

        // when
        // 지하철_노선_목록_조회_요청
        final ExtractableResponse<Response> response = 지하철_노선_목록_조회();

        // then
        // 지하철_노선_목록_응답됨
        // 지하철_노선_목록_포함됨
        final List<LineResponse> lineResponses = response.jsonPath().getList(".", LineResponse.class);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(lineResponses.size()).isEqualTo(2);
    }

    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void getLine() {
        // given
        // 지하철_노선_등록되어_있음
        지하철_노선_생성("신분당선","red");

        // when
        // 지하철_노선_조회_요청
        final ExtractableResponse<Response> lineResponse = 지하철_노선_목록_조회();

        // then
        // 지하철_노선_응답됨
        final List<LineResponse> lines = lineResponse.jsonPath().getList(".", LineResponse.class);
        assertThat(lineResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(lines.size()).isOne();
    }

    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLine() {
        // given
        // 지하철_노선_등록되어_있음
        final ExtractableResponse<Response> lineCreateResponse = 지하철_노선_생성("5호선","purple");
        final LineResponse lineResponse = lineCreateResponse.as(LineResponse.class);

        // when
        // 지하철_노선_수정_요청
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new LineRequest("9호선", "golden"))
                .pathParam("lineId", lineResponse.getId())
                .when().put("/lines/{lineId}")
                .then().log().all().extract();

        // then
        // 지하철_노선_수정됨
        final LineResponse line = response.as(LineResponse.class);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(line.getName()).isEqualTo("9호선");
        assertThat(line.getColor()).isEqualTo("golden");
    }

    @DisplayName("지하철 노선을 제거한다.")
    @Test
    void deleteLine() {
        // given
        // 지하철_노선_등록되어_있음
        final ExtractableResponse<Response> lineCreateResponse = 지하철_노선_생성("2호선", "green");
        final LineResponse lineResponse = lineCreateResponse.as(LineResponse.class);

        // when
        // 지하철_노선_제거_요청
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("lineId", lineResponse.getId())
                .when().delete("/lines/{lineId}")
                .then().log().all().extract();

        // then
        // 지하철_노선_삭제됨
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private ExtractableResponse<Response> 지하철_노선_목록_조회() {
        return RestAssured.given().log().all()
                .when().get("/lines")
                .then().log().all().extract();
    }

    private ExtractableResponse<Response> 지하철_노선_생성(String name, String color) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new LineRequest(name, color))
                .when().post("/lines")
                .then().log().all().extract();
    }
}
