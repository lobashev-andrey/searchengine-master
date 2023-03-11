package searchengine.services;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Stopper {
    private volatile boolean stop = true;
}
