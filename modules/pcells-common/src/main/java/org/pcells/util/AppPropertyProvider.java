package org.pcells.util;

import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by bernardt on 11/12/14 for pcells
 */
public class AppPropertyProvider {

    private final static Logger _logger = LoggerFactory.getLogger(AppPropertyProvider.class);

    private ClassPathResource _appPropertiesResource;
    private Properties _properties;
    private String _versionString;

    public AppPropertyProvider() {
        _appPropertiesResource = new ClassPathResource("app.properties");
        _properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = _appPropertiesResource.getInputStream();
            _properties.load(inputStream);
        }
        catch ( IOException e ) {
            _logger.error(e.getMessage(), e);
        }
        finally {
            Closeables.closeQuietly(inputStream);
        }
        _versionString = _properties.getProperty( "application.version" ) ;
    }

    public String getVersionString() {
        return _versionString;
    }
}
