package fm.pattern.tokamak.server.service;

import static fm.pattern.tokamak.server.PatternAssertions.assertThat;
import static fm.pattern.tokamak.server.dsl.AudienceDSL.audience;
import static fm.pattern.tokamak.server.dsl.ClientDSL.client;
import static fm.pattern.tokamak.server.dsl.GrantTypeDSL.grantType;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import fm.pattern.tokamak.server.IntegrationTest;
import fm.pattern.tokamak.server.model.Audience;
import fm.pattern.tokamak.server.model.Client;
import fm.pattern.tokamak.server.repository.Cache;
import fm.pattern.valex.EntityNotFoundException;
import fm.pattern.valex.ResourceConflictException;
import fm.pattern.valex.Result;
import fm.pattern.valex.UnprocessableEntityException;

public class AudienceServiceIntegrationTest extends IntegrationTest {

	@Autowired
	private AudienceService audienceService;

	@Autowired
	@Qualifier("clientCache")
	private Cache cache;

	@Test
	public void shouldBeAbleToCreateAnAudience() {
		Audience audience = new Audience("user");

		Result<Audience> result = audienceService.create(audience);
		assertThat(result).accepted();

		Audience created = result.getInstance();
		assertThat(created.getName()).isEqualTo(audience.getName());
		assertThat(created.getId()).isNotNull();
		assertThat(created.getCreated()).isNotNull();
		assertThat(created.getUpdated()).isNotNull();
		assertThat(created.getCreated()).isEqualTo(created.getUpdated());
	}

	@Test
	public void shouldNotBeAbleToCreateAnAudienceIfTheAudienceIsInvalid() {
		Audience audience = audience().withName(null).build();
		assertThat(audienceService.create(audience)).rejected().withMessage("An audience name is required.");
	}

	@Test
	public void shouldBeAbleToUpdateAnAudience() {
		Audience audience = audience().save();
		audience.setName("first");

		assertThat(audienceService.update(audience)).accepted();

		Audience found = audienceService.findById(audience.getId()).getInstance();
		assertThat(found.getName()).isEqualTo("first");
	}

	@Test
	public void shouldFlushTheClientCacheWhenAnAudienceIsUpdated() {
		Client client = client().withGrantType(grantType().save()).save();

		assertThat(cache.get("clients:id:" + client.getId(), Client.class)).isNotNull();
		assertThat(audienceService.update(audience().save())).accepted();
		assertThat(cache.get("clients:id:" + client.getId(), Client.class)).isNull();
	}

	@Test
	public void shouldNotBeAbleToUpdateAnAudienceIfTheAudienceIsInvalid() {
		Audience audience = audience().save();
		audience.setName(null);

		assertThat(audienceService.update(audience)).rejected().withMessage("An audience name is required.");
	}

	@Test
	public void shouldBeAbleToDeleteAnAudience() {
		Audience audience = audience().save();
		assertThat(audienceService.findById(audience.getId())).accepted();

		assertThat(audienceService.delete(audience)).accepted();
		assertThat(audienceService.findById(audience.getId())).rejected();
	}

	@Test
	public void shouldNotBeAbleToDeleteAnAudienceIfTheAudienceIsInvalid() {
		Audience audience = audience().save();
		audience.setId(null);
		assertThat(audienceService.delete(audience)).rejected().withError("ENT-0001", "An id is required.", UnprocessableEntityException.class);
	}

	@Test
	public void shouldNotBeAbleToDeleteAnAudienceIfTheAudienceIsBeingUsedByClients() {
		Audience audience = audience().save();
		client().withAudience(audience).withGrantType(grantType().save()).save();

		Result<Audience> result = audienceService.delete(audience);
		assertThat(result).rejected().withError("AUD-0005", "This audience cannot be deleted, 1 client is linked to this audience.", ResourceConflictException.class);
	}

	@Test
	public void shouldBeAbleToFindAnAudienceById() {
		Audience audience = audience().save();

		Result<Audience> result = audienceService.findById(audience.getId());
		assertThat(result).accepted();
		assertThat(result.getInstance()).isEqualToComparingFieldByField(audience);
	}

	@Test
	public void shouldNotBeAbleToFindAnAudienceByIdIfTheAudienceIdIsNullOrEmpty() {
		assertThat(audienceService.findById(null)).rejected().withError("AUD-0006", "An audience id is required.", UnprocessableEntityException.class);
		assertThat(audienceService.findById("")).rejected().withError("AUD-0006", "An audience id is required.", UnprocessableEntityException.class);
		assertThat(audienceService.findById("  ")).rejected().withError("AUD-0006", "An audience id is required.", UnprocessableEntityException.class);
	}

	@Test
	public void shouldNotBeAbleToFindAnAudienceByIdIfTheAudienceIdDoesNotExist() {
		assertThat(audienceService.findById("csrx")).rejected().withError("SYS-0001", "No such audience id: csrx", EntityNotFoundException.class);
	}

	@Test
	public void shouldBeAbleToFindAnAudienceByName() {
		Audience audience = audience().save();

		Result<Audience> result = audienceService.findByName(audience.getName());
		assertThat(result).accepted();
		assertThat(result.getInstance()).isEqualToComparingFieldByField(audience);
	}

	@Test
	public void shouldNotBeAbleToFindAnAudienceByNameIfTheAudienceNameIsNullOrEmpty() {
		assertThat(audienceService.findByName(null)).rejected().withError("AUD-0001", "An audience name is required.", UnprocessableEntityException.class);
		assertThat(audienceService.findByName("")).rejected().withError("AUD-0001", "An audience name is required.", UnprocessableEntityException.class);
		assertThat(audienceService.findByName("  ")).rejected().withError("AUD-0001", "An audience name is required.", UnprocessableEntityException.class);
	}

	@Test
	public void shouldNotBeAbleToFindAnAudienceByNameIfTheAudienceNameDoesNotExist() {
		assertThat(audienceService.findByName("csrx")).rejected().withError("AUD-0008", "No such audience name: csrx", EntityNotFoundException.class);
	}

	@Test
	public void shouldBeAbleToListAllAudiences() {
		range(0, 5).forEach(i -> audience().save());

		Result<List<Audience>> result = audienceService.list();
		assertThat(result).accepted();
		assertThat(result.getInstance().size()).isGreaterThanOrEqualTo(5);
	}

	@Test
	public void shouldBeAbleToFindMultipleAudiencesById() {
		Audience audience1 = audience().save();
		Audience audience2 = audience().save();
		Audience audience3 = audience().save();

		List<String> ids = new ArrayList<>();
		ids.add(audience1.getId());
		ids.add(audience2.getId());
		ids.add(audience3.getId());

		Result<List<Audience>> result = audienceService.findExistingById(ids);
		assertThat(result).accepted();
		assertThat(result.getInstance()).hasSize(3);
		assertThat(result.getInstance()).contains(audience1, audience2, audience3);
	}

	@Test
	public void shouldReturnAnEmptyListOfAudiencesIfTheAudienceIdListIsNullOrEmpty() {
		assertThat(audienceService.findExistingById(null)).accepted();
		assertThat(audienceService.findExistingById(null).getInstance()).isEmpty();

		assertThat(audienceService.findExistingById(new ArrayList<String>())).accepted();
		assertThat(audienceService.findExistingById(new ArrayList<String>()).getInstance()).isEmpty();
	}

	@Test
	public void shouldIgnoreEmptyAudienceEntries() {
		Audience audience1 = audience().save();
		Audience audience2 = audience().save();
		Audience audience3 = audience().save();

		List<String> ids = new ArrayList<>();
		ids.add(audience1.getId());
		ids.add(null);
		ids.add(audience2.getId());
		ids.add("");
		ids.add(audience3.getId());

		Result<List<Audience>> result = audienceService.findExistingById(ids);
		assertThat(result).accepted();
		assertThat(result.getInstance()).hasSize(3);
		assertThat(result.getInstance()).contains(audience1, audience2, audience3);
	}

	@Test
	public void shouldReturnAnEmtpyListWhenAllAudiencesAreNullOrEmpty() {
		List<String> ids = new ArrayList<>();
		ids.add(null);
		ids.add("");

		Result<List<Audience>> result = audienceService.findExistingById(ids);
		assertThat(result).accepted();

		assertThat(result.getInstance()).isEmpty();
	}
}
