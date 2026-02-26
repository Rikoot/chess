package model.results;

import model.GameData;
import java.util.Collection;

public record ListResult(Collection<GameData> games) {
}
