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

        public String label;
        public String path;
        public Integer type;
        public String icon;
        public Integer is_accessible;
        public List<MenuTreeDto> children = new ArrayList<>();

}
