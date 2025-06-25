package id.co.msiglife.pollux.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuTreeDto {

        public Long lsmpId;
        public String label;
        public String path;
        public Integer type;
        public String icon;
        public List<MenuTreeDto> children = new ArrayList<>();

}
