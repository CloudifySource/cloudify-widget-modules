package cloudify.widget.website.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by sefi on 9/2/14.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Pool couldn't be found.")
public class PoolNotFoundException extends RuntimeException {
}
