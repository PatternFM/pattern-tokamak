package fm.pattern.jwt.spec.endpoints;

import static fm.pattern.jwt.spec.PatternAssertions.assertThat;
import static fm.pattern.tokamak.sdk.dsl.AccessTokenDSL.token;
import static fm.pattern.tokamak.sdk.dsl.AuthorityDSL.authority;
import static fm.pattern.tokamak.sdk.dsl.ClientDSL.client;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import fm.pattern.jwt.spec.AcceptanceTest;
import fm.pattern.tokamak.sdk.AuthoritiesClient;
import fm.pattern.tokamak.sdk.JwtClientProperties;
import fm.pattern.tokamak.sdk.commons.Result;
import fm.pattern.tokamak.sdk.model.AccessTokenRepresentation;
import fm.pattern.tokamak.sdk.model.AuthoritiesRepresentation;
import fm.pattern.tokamak.sdk.model.AuthorityRepresentation;

public class AuthoritiesEndpointAcceptanceTest extends AcceptanceTest {

	private final AuthoritiesClient client = new AuthoritiesClient(JwtClientProperties.getEndpoint());

	private AccessTokenRepresentation token;

	@Before
	public void before() {
		this.token = token().withClient(TEST_CLIENT_CREDENTIALS).thatIs().persistent().build();
	}

	@Test
	public void shouldBeAbleToCreateAnAuthority() {
		AuthorityRepresentation authority = authority().build();

		Result<AuthorityRepresentation> result = client.create(authority, token.getAccessToken());
		assertThat(result).accepted().withResponseCode(201);

		AuthorityRepresentation created = result.getInstance();
		assertThat(created.getId()).startsWith("ath_");
		assertThat(created.getCreated()).isNotNull();
		assertThat(created.getUpdated()).isNotNull();
		assertThat(created.getCreated()).isEqualTo(created.getUpdated());
		assertThat(created.getName()).isEqualTo(authority.getName());
		assertThat(created.getDescription()).isEqualTo(authority.getDescription());
	}

	@Test
	public void shouldNotBeAbleToCreateAnAuthorityIfTheAuthorityIsInvalid() {
		AuthorityRepresentation authority = authority().withName("").build();

		Result<AuthorityRepresentation> result = client.create(authority, token.getAccessToken());
		assertThat(result).rejected().withResponseCode(422).withCode("ATH-0001").withMessage("An authority name is required.");
	}

	@Test
	public void shouldNotBeAbleToCreateAnAuthorityIfTheAuthorityNameIsAlreadyInUse() {
		AuthorityRepresentation authority = authority().thatIs().persistent(token).build();

		Result<AuthorityRepresentation> result = client.create(authority, token.getAccessToken());
		assertThat(result).rejected().withResponseCode(409).withCode("ATH-0003").withMessage("This authority name is already in use.");
	}

	@Test
	public void shouldBeAbleToUpdateAnAuthority() {
		AuthorityRepresentation authority = authority().thatIs().persistent(token).build();
		pause(1000);

		authority.setName(RandomStringUtils.randomAlphabetic(10));

		Result<AuthorityRepresentation> result = client.update(authority, token.getAccessToken());
		assertThat(result).accepted().withResponseCode(200);

		AuthorityRepresentation updated = result.getInstance();
		assertThat(updated.getId()).startsWith("ath_");
		assertThat(updated.getCreated()).isNotNull();
		assertThat(updated.getUpdated()).isNotNull();

		assertThat(updated.getCreated()).isEqualTo(authority.getCreated());
		assertThat(updated.getCreated()).isBefore(updated.getUpdated());
		assertThat(updated.getUpdated()).isAfter(authority.getUpdated());

		assertThat(updated.getName()).isEqualTo(authority.getName());
	}

	@Test
	public void shouldNotBeAbleToUpdateAnAuthorityIfTheAuthorityIsInvalid() {
		AuthorityRepresentation authority = authority().thatIs().persistent(token).build();
		authority.setName("");

		Result<AuthorityRepresentation> result = client.update(authority, token.getAccessToken());
		assertThat(result).rejected().withResponseCode(422).withCode("ATH-0001").withMessage("An authority name is required.");
	}

	@Test
	public void shouldNotBeAbleToUpdateAnAuthorityIfTheAuthorityNameIsAlreadyInUse() {
		AuthorityRepresentation existing = authority().thatIs().persistent(token).build();
		AuthorityRepresentation authority = authority().thatIs().persistent(token).build();
		authority.setName(existing.getName());

		Result<AuthorityRepresentation> result = client.update(authority, token.getAccessToken());
		assertThat(result).rejected().withResponseCode(409).withCode("ATH-0003").withMessage("This authority name is already in use.");
	}

	@Test
	public void shouldBeAbleToDeleteAnAuthority() {
		AuthorityRepresentation authority = authority().thatIs().persistent(token).build();

		Result<AuthorityRepresentation> result = client.delete(authority.getId(), token.getAccessToken());
		assertThat(result).accepted().withResponseCode(204);

		assertThat(client.findById(authority.getId(), token.getAccessToken())).rejected().withResponseCode(404);
	}

	@Test
	public void shouldNotBeAbleToDeleteAnAuthorityIfTheAuthorityIsAssociatedWithClients() {
		AuthorityRepresentation authority = authority().thatIs().persistent(token).build();
		client().withAuthorities(authority).withGrantTypes("password", "refresh_token").thatIs().persistent(token).build();

		Result<AuthorityRepresentation> result = client.delete(authority.getId(), token.getAccessToken());
		assertThat(result).rejected().withResponseCode(409).withCode("ATH-0005").withMessage("This authority cannot be deleted, 1 client is linked to this authority.");
	}

	@Test
	public void shouldBeAbleToFindAnAuthorityById() {
		AuthorityRepresentation authority = authority().thatIs().persistent(token).build();

		Result<AuthorityRepresentation> result = client.findById(authority.getId(), token.getAccessToken());
		assertThat(result).accepted().withResponseCode(200);
		assertThat(result.getInstance()).isEqualToComparingFieldByField(authority);
	}

	@Test
	public void shouldReturnA404WhenAnAuthorityWithTheSpecifiedIdCannotBeFound() {
		Result<AuthorityRepresentation> result = client.findById("ath_123", token.getAccessToken());
		assertThat(result).rejected().withResponseCode(404).withMessage("No such authority id: ath_123");
	}

	@Test
	public void shouldBeAbleToFindAnAuthorityByName() {
		AuthorityRepresentation authority = authority().thatIs().persistent(token).build();

		Result<AuthorityRepresentation> result = client.findByName(authority.getName(), token.getAccessToken());
		assertThat(result).accepted().withResponseCode(200);
		assertThat(result.getInstance()).isEqualToComparingFieldByField(authority);
	}

	@Test
	public void shouldReturnA404WhenAnAuthorityWithTheSpecifiedNameCannotBeFound() {
		Result<AuthorityRepresentation> result = client.findByName("rol_123", token.getAccessToken());
		assertThat(result).rejected().withResponseCode(404).withMessage("No such authority name: rol_123");
	}

	@Test
	public void shouldBeAbleToListAuthorities() {
		IntStream.range(1, 5).forEach(i -> authority().withToken(token).thatIs().persistent().build());

		Result<AuthoritiesRepresentation> result = client.list(token.getAccessToken());
		assertThat(result).accepted().withResponseCode(200);
		assertThat(result.getInstance().getAuthorities().size()).isGreaterThanOrEqualTo(5);
	}
	
}
