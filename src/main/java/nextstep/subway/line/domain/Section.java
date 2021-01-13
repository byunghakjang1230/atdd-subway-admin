package nextstep.subway.line.domain;

import lombok.Builder;
import nextstep.subway.station.domain.Station;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;


@Entity
@Table(indexes = {
        @Index(unique = true, columnList = "up_station_id,down_station_id")
})
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Line line;

    @ManyToOne(fetch = FetchType.LAZY)
    private Station upStation;

    @ManyToOne(fetch = FetchType.LAZY)
    private Station downStation;

    @Embedded
    private Distance distance;

    protected Section() {
    }

    @Builder
    private Section(Line line, Station upStation, Station downStation, int distance) {
        validate(line, upStation, downStation);
        this.line = line;
        this.upStation = upStation;
        this.downStation = downStation;
        this.distance = new Distance(distance);
    }

    public void updateUpStation(Station upStation) {
        this.upStation = upStation;
    }

    public void updateDownStation(Station downStation) {
        this.downStation = downStation;
    }

    private void validate(Line line, Station upStation, Station downStation) {
        validateRequired(line, upStation, downStation);
    }

    private void validateRequired(Line line, Station upStation, Station downStation) {
        if (line == null || upStation == null || downStation == null) {
            throw new IllegalArgumentException("필수값 누락입니다.");
        }
    }

    public void replaceDownStation(Section section) {
        this.distance.calculateMinus(section.getDistance());
        updateDownStation(section.getUpStation());
    }

    public void replaceUpStation(Section section) {
        this.distance.calculateMinus(section.getDistance());
        updateUpStation(section.getDownStation());
    }

    public void replaceSection(Section targetSection) {
        this.distance.calculatePlus(targetSection.getDistance());
        updateUpStation(targetSection.getUpStation());
    }

    public boolean isSameDownStation(Station station) {
        return this.downStation == station;
    }

    public boolean isSameUpStation(Station station) {
        return this.upStation == station;
    }

    public Station getUpStation() {
        return upStation;
    }

    public Station getDownStation() {
        return downStation;
    }

    public Distance getDistance() {
        return distance;
    }

    public List<Station> getStations() {
        return Arrays.asList(upStation, downStation);
    }
}