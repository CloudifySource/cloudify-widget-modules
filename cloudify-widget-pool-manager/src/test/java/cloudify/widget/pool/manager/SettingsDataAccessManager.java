package cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.dto.ManagerSettings;

/**
 * User: eliranm
 * Date: 2/27/14
 * Time: 3:13 PM
 */
public interface SettingsDataAccessManager {

    ManagerSettings read();
}
