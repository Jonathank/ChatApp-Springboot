package app.chat.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
    public class GroupDTO {
	    private Long id;
	    private String groupname;
	    private ImageDTO image;
	    private List<UserDTO> members;
	    private Long creatorId;
	    private String message; // Used for error responses, e.g., in GroupController
	}
