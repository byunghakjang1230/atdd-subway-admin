package nextstep.subway.section.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import nextstep.subway.line.domain.Line;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationResponse;

/**
 * Sections 클래스의 기능 검증 테스트
 */
public class SectionsTest {

    private Line line;
    private Line blueLine;
    private Station station2;
    private Station station3;
    private Station station1;
    private Station station4;
    private Station station5;
    private List<Station> lineStations;
    private Section section1;
    private Section section2;
    private Section section3;
    private Section section4;
    private Sections sections;

    @BeforeEach
    void setUp() {
        line = new Line("2호선", "green");
        blueLine = new Line("4호선", "blue");
        station2 = new Station("역삼역");
        station3 = new Station("교대역");
        station1 = new Station("강남역");
        station4 = new Station("서초역");
        station5 = new Station("방배역");
        lineStations = Arrays.asList(station2, station3, station1, station4, station5);
        section1 = new Section(station2, station3, 3, line);
        section2 = new Section(station1, station4, 10, line);
        section3 = new Section(station4, station5, 3, line);
        section4 = new Section(station3, station1, 3, line);
        sections = new Sections(Arrays.asList(section1, section2, section3, section4));
    }

    @Test
    @DisplayName("상행역에서 하행역으로 정렬된 역 목록 반환")
    void getStationResponses() {
        // when
        List<Station> createStations = sections.getSortedStations();

        // then
        List<StationResponse> targetStationResponses = lineStations.stream()
                .map(Station::toStationResponse)
                .collect(Collectors.toList());
        List<StationResponse> resultStationResponses = createStations.stream()
                .map(Station::toStationResponse)
                .collect(Collectors.toList());
        assertThat(Arrays.equals(resultStationResponses.toArray(), targetStationResponses.toArray())).isTrue();
    }

    @Test
    @DisplayName("일급 컬렉션 초기화 유효성 검증")
    void sections_is_empty_exception() {
        // then
        assertThatThrownBy(() -> new Sections(new ArrayList<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("구간 목록은 하나 이상의 구간으로 구성되어야 합니다.");
    }

    @Test
    @DisplayName("신규 메서드 중복 노선 예외처리")
    void new_addSection_exception_case1() {
        // given
        Section section1 = new Section(station1, station2, 2, blueLine);
        Section section2 = new Section(station2, station3, 3, blueLine);
        Section section3 = new Section(station3, station4, 4, blueLine);
        Section addSection1 = new Section(station1, station4, 6, blueLine);
        Section addSection2 = new Section(station3, station2, 6, blueLine);
        Sections sections = new Sections(Arrays.asList(section1, section2, section3));

        // then
        assertAll(
                () -> assertThatThrownBy(() -> sections.addSection(addSection1))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("상행역, 하행역이 이미 존재합니다."),
                () -> assertThatThrownBy(() -> sections.addSection(addSection2))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("상행역, 하행역이 이미 존재합니다.")
        );
    }

    @Test
    @DisplayName("신규 메서드 중복 구간길이 예외처리")
    void new_addSection_exception_case2() {
        // given
        Section section1 = new Section(station1, station2, 2, blueLine);
        Section section2 = new Section(station2, station3, 3, blueLine);
        Section section3 = new Section(station3, station4, 4, blueLine);
        Section addSection1 = new Section(station1, station5, 5, blueLine);
        Section addSection2 = new Section(station3, station5, 4, blueLine);
        Sections sections = new Sections(Arrays.asList(section1, section2, section3));

        // then
        assertAll(
                () -> assertThatThrownBy(() -> sections.addSection(addSection1))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("같은 길이의 구간이 존재합니다."),
                () -> assertThatThrownBy(() -> sections.addSection(addSection2))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("같은 길이의 구간이 존재합니다.")
        );
    }

    @Test
    @DisplayName("신규 메소드 - 추가 구간의 하행역이 기존구간의 가장 앞과, 상행역이 기존구간의 가장 뒤와 같을 경우")
    void new_addSection_front_or_end_case() {
        // given
        Section section1 = new Section(station1, station2, 2, blueLine);
        Section section2 = new Section(station2, station3, 3, blueLine);
        Section frontSection = new Section(station4, station1, 3, blueLine);
        Section endSection = new Section(station3, station5, 3, blueLine);
        Sections sections = new Sections(Arrays.asList(section1, section2));

        // when
        sections.addSection(frontSection);
        sections.addSection(endSection);

        // then
        List<String> resultStationNames = sections.getSortedStations()
                .stream()
                .map(station -> station.toStationResponse().getName()).collect(Collectors.toList());
        List<String> targetStationNames = Arrays.asList(station4, station1, station2, station3, station5)
                .stream()
                .map(station -> station.toStationResponse().getName()).collect(Collectors.toList());
        assertThat(Arrays.equals(resultStationNames.toArray(), targetStationNames.toArray())).isTrue();
    }

    @Test
    @DisplayName("신규 메소드 - 상행역이 이미 존재하고 추가 구간이 기존 구간 중간에 위치하는 경우")
    void new_addSection_case1() {
        // given
        Section section1 = new Section(station1, station2, 2, blueLine);
        Section section2 = new Section(station2, station3, 3, blueLine);
        Section section3 = new Section(station3, station4, 4, blueLine);
        Section addSection = new Section(station1, station5, 6, blueLine);
        Sections sections = new Sections(Arrays.asList(section1, section2, section3));

        // when
        Section section = sections.addSection(addSection);

        // then
        assertAll(
                () -> assertThat(section.getUpStation()).isSameAs(station3),
                () -> assertThat(section.getDownStation()).isSameAs(station5),
                () -> assertThat(section.getDistance().isEqualTo(1)).isTrue(),
                () -> assertThat(section3.getUpStation()).isSameAs(station5),
                () -> assertThat(section3.getDownStation()).isSameAs(station4),
                () -> assertThat(section3.getDistance().isEqualTo(3)).isTrue()
        );
    }

    @Test
    @DisplayName("신규 메소드 - 상행역이 이미 존재하고 추가 구간이 기존 구간의 바깥에 위치하는 경우")
    void new_addSection_case2() {
        // given
        Section section1 = new Section(station1, station2, 2, blueLine);
        Section section2 = new Section(station2, station3, 3, blueLine);
        Section section3 = new Section(station3, station4, 4, blueLine);
        Section addSection = new Section(station1, station5, 12, blueLine);
        Sections sections = new Sections(Arrays.asList(section1, section2, section3));

        // when
        Section resultSection = sections.addSection(addSection);

        // then
        assertAll(
                () -> assertThat(resultSection.getUpStation()).isSameAs(station4),
                () -> assertThat(resultSection.getDownStation()).isSameAs(station5),
                () -> assertThat(resultSection.getDistance().isEqualTo(3)).isTrue(),
                () -> assertThat(section3.getUpStation()).isSameAs(station3),
                () -> assertThat(section3.getDownStation()).isSameAs(station4),
                () -> assertThat(section3.getDistance().isEqualTo(4)).isTrue()
        );
    }

    @Test
    @DisplayName("신규 메소드 - 하행역이 이미 존재하고 추가 구간이 기존 구간 중간에 위치하는 경우")
    void new_addSection_case3() {
        // given
        Section section1 = new Section(station1, station2, 2, blueLine);
        Section section2 = new Section(station2, station3, 3, blueLine);
        Section section3 = new Section(station3, station4, 4, blueLine);
        Section addSection = new Section(station5, station3, 4, blueLine);
        Sections sections = new Sections(Arrays.asList(section1, section2, section3));

        // when
        Section resultSection = sections.addSection(addSection);

        // then
        assertAll(
                () -> assertThat(resultSection.getUpStation()).isSameAs(station5),
                () -> assertThat(resultSection.getDownStation()).isSameAs(station2),
                () -> assertThat(resultSection.getDistance().isEqualTo(1)).isTrue(),
                () -> assertThat(section1.getUpStation()).isSameAs(station1),
                () -> assertThat(section1.getDownStation()).isSameAs(station5),
                () -> assertThat(section1.getDistance().isEqualTo(1)).isTrue()
        );
    }

    @Test
    @DisplayName("신규 메소드 - 하행역이 이미 존재하고 추가 구간이 기존 구간 바깥에 위치하는 경우")
    void new_addSection_case4() {
        // given
        Section section1 = new Section(station1, station2, 2, blueLine);
        Section section2 = new Section(station2, station3, 3, blueLine);
        Section section3 = new Section(station3, station4, 4, blueLine);
        Section addSection = new Section(station5, station3, 11, blueLine);
        Sections sections = new Sections(Arrays.asList(section1, section2, section3));

        // when
        Section resultSection = sections.addSection(addSection);

        // then
        assertAll(
                () -> assertThat(resultSection.getUpStation()).isSameAs(station5),
                () -> assertThat(resultSection.getDownStation()).isSameAs(station1),
                () -> assertThat(resultSection.getDistance().isEqualTo(6)).isTrue(),
                () -> assertThat(section1.getUpStation()).isSameAs(station1),
                () -> assertThat(section1.getDownStation()).isSameAs(station2),
                () -> assertThat(section1.getDistance().isEqualTo(2)).isTrue()
        );
    }
}
