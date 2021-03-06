import javax.inject._

import play.api.http.DefaultHttpFilters
import play.filters.cors.CORSFilter
import play.filters.headers.SecurityHeadersFilter
import play.filters.hosts.AllowedHostsFilter

/**
 * Add the following filters by default to all projects
 * 
 * https://www.playframework.com/documentation/latest/ScalaCsrf 
 * https://www.playframework.com/documentation/latest/AllowedHostsFilter
 * https://www.playframework.com/documentation/latest/SecurityHeaders
 */
@Singleton
class Filters @Inject() (
  allowedHostsFilter: AllowedHostsFilter,
  securityHeadersFilter: SecurityHeadersFilter,
  corsFilter: CORSFilter
) extends DefaultHttpFilters(
  allowedHostsFilter, 
  securityHeadersFilter,
  corsFilter
)