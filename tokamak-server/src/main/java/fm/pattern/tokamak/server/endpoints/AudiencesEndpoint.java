/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fm.pattern.tokamak.server.endpoints;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fm.pattern.tokamak.authorization.Authorize;
import fm.pattern.tokamak.sdk.model.AudienceRepresentation;
import fm.pattern.tokamak.sdk.model.AudiencesRepresentation;
import fm.pattern.tokamak.server.conversion.AudienceConversionService;
import fm.pattern.tokamak.server.model.Audience;
import fm.pattern.tokamak.server.service.AudienceService;

@RestController
public class AudiencesEndpoint extends Endpoint {

	private final AudienceService audienceService;
	private final AudienceConversionService converter;

	@Autowired
	public AudiencesEndpoint(AudienceService audienceService, AudienceConversionService converter) {
		this.audienceService = audienceService;
		this.converter = converter;
	}

	@Authorize(scopes = "audiences:create")
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value = "/v1/audiences", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public AudienceRepresentation create(@RequestBody AudienceRepresentation representation) {
		Audience audience = converter.convert(representation);
		Audience created = audienceService.create(audience).orThrow();
		return converter.convert(audienceService.findById(created.getId()).orThrow());
	}

	@Authorize(scopes = "audiences:update")
	@RequestMapping(value = "/v1/audiences/{id}", method = PUT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public AudienceRepresentation update(@PathVariable String id, @RequestBody AudienceRepresentation representation) {
		Audience audience = converter.convert(representation, audienceService.findById(id).orThrow());
		return converter.convert(audienceService.update(audience).orThrow());
	}

	@Authorize(scopes = "audiences:delete")
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/v1/audiences/{id}", method = DELETE)
	public void delete(@PathVariable String id) {
		Audience audience = audienceService.findById(id).orThrow();
		audienceService.delete(audience).orThrow();
	}

	@Authorize(scopes = "audiences:read")
	@RequestMapping(value = "/v1/audiences/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
	public AudienceRepresentation findById(@PathVariable String id) {
		Audience audience = audienceService.findById(id).orThrow();
		return converter.convert(audience);
	}

	@Authorize(scopes = "audiences:read")
	@RequestMapping(value = "/v1/audiences/name/{name}", method = GET, produces = APPLICATION_JSON_VALUE)
	public AudienceRepresentation findByName(@PathVariable String name) {
		Audience audience = audienceService.findByName(name).orThrow();
		return converter.convert(audience);
	}

	@Authorize(scopes = "audiences:read")
	@RequestMapping(value = "/v1/audiences", method = GET, produces = APPLICATION_JSON_VALUE)
	public AudiencesRepresentation list() {
		List<Audience> audiences = audienceService.list().orThrow();
		return new AudiencesRepresentation(audiences.stream().map(audience -> converter.convert(audience)).collect(Collectors.toList()));
	}

}
