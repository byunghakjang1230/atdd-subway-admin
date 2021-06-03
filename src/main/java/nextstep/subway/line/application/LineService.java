package nextstep.subway.line.application;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;

@Service
@Transactional
public class LineService {
    private LineRepository lineRepository;

    public LineService(LineRepository lineRepository) {
        this.lineRepository = lineRepository;
    }

    public LineResponse saveLine(LineRequest request) {
        Line persistLine = lineRepository.save(request.toLine());
        return LineResponse.of(persistLine);
    }

    public List<LineResponse> findAllLines() {
        return lineRepository.findAll()
                .stream()
                .map(line -> LineResponse.of(line))
                .collect(Collectors.toList());
    }

    public LineResponse findLineById(long id) throws NoSuchElementException {
        return LineResponse.of(findLineByIdOrThrow(id, "노선이 존재하지 않습니다."));
    }

    public void updateLine(long id, LineRequest updateLineRequest) {
        Line line = findLineByIdOrThrow(id, "수정 대상 노선이 존재하지 않습니다.");
        line.update(updateLineRequest.toLine());
    }

    public void deleteLineById(Long id) {
        Line line = findLineByIdOrThrow(id, "삭제 대상 노선이 존재하지 않습니다.");
        lineRepository.delete(line);
    }

    private Line findLineByIdOrThrow(Long id, String throwMessage) {
        return lineRepository.findById(id).orElseThrow(() -> new NoSuchElementException(throwMessage));
    }
}
