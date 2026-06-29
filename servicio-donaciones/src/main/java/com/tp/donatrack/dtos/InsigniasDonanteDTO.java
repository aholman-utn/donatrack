package com.tp.donatrack.dtos;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsigniasDonanteDTO {
    private List<InsigniaDTO> insignias;
    private int totalInsignias;
}
