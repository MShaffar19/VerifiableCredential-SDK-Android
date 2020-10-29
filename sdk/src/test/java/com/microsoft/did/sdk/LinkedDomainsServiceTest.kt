// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainUnVerified
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.did.sdk.credential.service.models.serviceResponses.LinkedDomainsResponse
import com.microsoft.did.sdk.credential.service.validators.JwtDomainLinkageCredentialValidator
import com.microsoft.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierResponse
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.serializer.Serializer
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LinkedDomainsServiceTest {
    private val serializer = Serializer()
    private val mockedResolver: Resolver = mockk()
    private val mockedJwtValidator: JwtValidator = mockk()
    private val mockedJwtDomainLinkageCredentialValidator: JwtDomainLinkageCredentialValidator
    private val linkedDomainsService: LinkedDomainsService

    init {
        mockedJwtDomainLinkageCredentialValidator = JwtDomainLinkageCredentialValidator(mockedJwtValidator, serializer)
        linkedDomainsService = spyk(LinkedDomainsService(mockk(relaxed = true), mockedResolver, mockedJwtDomainLinkageCredentialValidator))
    }

    @Test
    fun `test linked domains with single domain as string successfully`() {
        val suppliedDidWithSingleServiceEndpoint = "did:ion:EiA8HR28m5KUig9elPRXkmKvvBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVDV5MG5nNTJCNzVjYWFqWU9qVjBRMmpxSng0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eXZLa3lvRmJsUWp0OXllU2J3TXkwR083MFM1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSWJsRWFpRUhtRU5Kc2h6czhtY1hJd2hTV0Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobDJtbVFPLS1GTzNKa2JrekVrIiwieSI6InBDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19"
        val expectedResponseString =
            """{"@context":"https://www.w3.org/ns/did-resolution/v1","didDocument":{"id":"did:ion:EiA8HR28m5KUig9elPRXkmKvvBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVDV5MG5nNTJCNzVjYWFqWU9qVjBRMmpxSng0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eXZLa3lvRmJsUWp0OXllU2J3TXkwR083MFM1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSWJsRWFpRUhtRU5Kc2h6czhtY1hJd2hTV0Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobDJtbVFPLS1GTzNKa2JrekVrIiwieSI6InBDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19","@context":["https://www.w3.org/ns/did/v1",{"@base":"did:ion:EiA8HR28m5KUig9elPRXkmKvvBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVDV5MG5nNTJCNzVjYWFqWU9qVjBRMmpxSng0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eXZLa3lvRmJsUWp0OXllU2J3TXkwR083MFM1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSWJsRWFpRUhtRU5Kc2h6czhtY1hJd2hTV0Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobDJtbVFPLS1GTzNKa2JrekVrIiwieSI6InBDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19"}],"service":[{"id":"#linkeddomains","type":"LinkedDomains","serviceEndpoint":"https://issuertestng.com"}],"publicKey":[{"id":"#sig_ed2c5edf","controller":"","type":"EcdsaSecp256k1VerificationKey2019","publicKeyJwk":{"kty":"EC","crv":"secp256k1","x":"hO8XixmYLM9UU2aV9odselH3hl2mmQO--FO3JkbkzEk","y":"pCXJqmzTo5PBGQLDbntmuAZHIYBqY8mCeWdihioKFRc"}}],"authentication":["#sig_ed2c5edf"]},"methodMetadata":{"published":false,"recoveryCommitment":"EiDl8DxyvKkyoFblQjt9yeSbwMy0GO70S5GaTSQtRQthRQ","updateCommitment":"EiDaWKk2r2bIblEaiEHmENJshzs8mcXIwhSWFubkVBRwYg"},"resolverMetadata":{"driverId":"did:ion","driver":"HttpDriver","retrieved":"2020-10-25T06:39:09.486Z","duration":"117.3589ms"}}"""
        val expectedResponse = serializer.parse(IdentifierResponse.serializer(), expectedResponseString)
        val expectedWellKnownConfigDocumentResponse = """{"@context":"https://identity.foundation/.well-known/contexts/did-configuration-v0.0.jsonld","linked_dids":["eyJhbGciOiJFUzI1NksiLCJraWQiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkjc2lnX2VkMmM1ZWRmIn0.eyJzdWIiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJpc3MiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJuYmYiOjE2MDM0MTU2NjQsImV4cCI6MjM5MjMzNDA2NCwidmMiOnsiQGNvbnRleHQiOlsiaHR0cHM6Ly93d3cudzMub3JnLzIwMTgvY3JlZGVudGlhbHMvdjEiLCJodHRwczovL2lkZW50aXR5LmZvdW5kYXRpb24vLndlbGwta25vd24vY29udGV4dHMvZGlkLWNvbmZpZ3VyYXRpb24tdjAuMC5qc29ubGQiXSwiaXNzdWVyIjoiZGlkOmlvbjpFaUE4SFIyOG01S1VpZzllbFBSWGttS3Z2QkdYY09veHBVckNzY1RkR0pjSVhRPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9ZWE5vSWpvaVJXbERWRFY1TUc1bk5USkNOelZqWVdGcVdVOXFWakJSTW1weFNuZzBORFpTYWpoUlRqRnBhSGR0ZVVwSlp5SXNJbkpsWTI5MlpYSjVYMk52YlcxcGRHMWxiblFpT2lKRmFVUnNPRVI0ZVhaTGEzbHZSbUpzVVdwME9YbGxVMkozVFhrd1IwODNNRk0xUjJGVVUxRjBVbEYwYUZKUkluMC5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFJHRlhTMnN5Y2pKaVNXSnNSV0ZwUlVodFJVNUtjMmg2Y3podFkxaEpkMmhUVjBaMVltdFdRbEozV1djaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ3SW1sa0lqb2ljMmxuWDJWa01tTTFaV1JtSWl3aWRIbHdaU0k2SWtWalpITmhVMlZqY0RJMU5tc3hWbVZ5YVdacFkyRjBhVzl1UzJWNU1qQXhPU0lzSW1wM2F5STZleUpyZEhraU9pSkZReUlzSW1OeWRpSTZJbk5sWTNBeU5UWnJNU0lzSW5naU9pSm9UemhZYVhodFdVeE5PVlZWTW1GV09XOWtjMlZzU0ROb2JESnRiVkZQTFMxR1R6TkthMkpyZWtWcklpd2llU0k2SW5CRFdFcHhiWHBVYnpWUVFrZFJURVJpYm5SdGRVRmFTRWxaUW5GWk9HMURaVmRrYVdocGIwdEdVbU1pZlN3aWNIVnljRzl6WlNJNld5SmhkWFJvSWl3aVoyVnVaWEpoYkNKZGZWMTlmVjE5IiwiaXNzdWFuY2VEYXRlIjoiMjAyMC0xMC0yM1QwMToxNDoyNC43NzRaIiwiZXhwaXJhdGlvbkRhdGUiOiIyMDQ1LTEwLTIzVDAxOjE0OjI0Ljc3NFoiLCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiRG9tYWluTGlua2FnZUNyZWRlbnRpYWwiXSwiY3JlZGVudGlhbFN1YmplY3QiOnsiaWQiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJvcmlnaW4iOiJodHRwczovL2lzc3VlcnRlc3RuZy5jb20ifX19.luP4vIE30u_kj5CINDhbjZcsHDbEEXS5hMFnzd7qVKs2OtseDJlYcgLOgbCqI17v3edXx9OVkErQ-sSuIGvt8g"]}"""
        val expectedWellKnownConfigDocument = serializer.parse(LinkedDomainsResponse.serializer(), expectedWellKnownConfigDocumentResponse)
        val expectedDomainUrl = "https://issuertestng.com"
        coEvery { mockedResolver.resolve(suppliedDidWithSingleServiceEndpoint) } returns Result.Success(expectedResponse.didDocument)
        coEvery { linkedDomainsService["getWellKnownConfigDocument"](expectedDomainUrl) } returns Result.Success(expectedWellKnownConfigDocument)
        coEvery { mockedJwtValidator.verifySignature(any()) } returns true
        runBlocking {
            val linkedDomainsResult = linkedDomainsService.fetchAndVerifyLinkedDomains(suppliedDidWithSingleServiceEndpoint)
            assertThat(linkedDomainsResult).isInstanceOf(Result.Success::class.java)
            assertThat((linkedDomainsResult as Result.Success).payload).isInstanceOf(LinkedDomainVerified::class.java)
            assertThat((linkedDomainsResult.payload as LinkedDomainVerified).domainUrl).isEqualTo(expectedDomainUrl)
        }
    }

    @Test
    fun `test linked domains with an array of domains successfully`() {
        val suppliedDidWithMultipleServiceEndpoints = "did:ion:EiA8HR28m5KUig9elPRXkmKvvBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVDV5MG5nNTJCNzVjYWFqWU9qVjBRMmpxSng0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eXZLa3lvRmJsUWp0OXllU2J3TXkwR083MFM1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSWJsRWFpRUhtRU5Kc2h6czhtY1hJd2hTV0Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobDJtbVFPLS1GTzNKa2JrekVrIiwieSI6InBDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19"
        val expectedResponseString =
            """{"@context":"https://www.w3.org/ns/did-resolution/v1","didDocument":{"id":"did:ion:EiA8HR28m5KUig9elPRXkmKvvBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVDV5MG5nNTJCNzVjYWFqWU9qVjBRMmpxSng0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eXZLa3lvRmJsUWp0OXllU2J3TXkwR083MFM1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSWJsRWFpRUhtRU5Kc2h6czhtY1hJd2hTV0Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobDJtbVFPLS1GTzNKa2JrekVrIiwieSI6InBDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19","@context":["https://www.w3.org/ns/did/v1",{"@base":"did:ion:EiA8HR28m5KUig9elPRXkmKvvBGXcOoxpUrCscTdGJcIXQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDVDV5MG5nNTJCNzVjYWFqWU9qVjBRMmpxSng0NDZSajhRTjFpaHdteUpJZyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaURsOER4eXZLa3lvRmJsUWp0OXllU2J3TXkwR083MFM1R2FUU1F0UlF0aFJRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGFXS2sycjJiSWJsRWFpRUhtRU5Kc2h6czhtY1hJd2hTV0Z1YmtWQlJ3WWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnX2VkMmM1ZWRmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoTzhYaXhtWUxNOVVVMmFWOW9kc2VsSDNobDJtbVFPLS1GTzNKa2JrekVrIiwieSI6InBDWEpxbXpUbzVQQkdRTERibnRtdUFaSElZQnFZOG1DZVdkaWhpb0tGUmMifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19"}],"service":[{"id":"#linkeddomains","type":"LinkedDomains","serviceEndpoint": {"origins":["https://issuertestng.com"]}}],"publicKey":[{"id":"#sig_ed2c5edf","controller":"","type":"EcdsaSecp256k1VerificationKey2019","publicKeyJwk":{"kty":"EC","crv":"secp256k1","x":"hO8XixmYLM9UU2aV9odselH3hl2mmQO--FO3JkbkzEk","y":"pCXJqmzTo5PBGQLDbntmuAZHIYBqY8mCeWdihioKFRc"}}],"authentication":["#sig_ed2c5edf"]},"methodMetadata":{"published":false,"recoveryCommitment":"EiDl8DxyvKkyoFblQjt9yeSbwMy0GO70S5GaTSQtRQthRQ","updateCommitment":"EiDaWKk2r2bIblEaiEHmENJshzs8mcXIwhSWFubkVBRwYg"},"resolverMetadata":{"driverId":"did:ion","driver":"HttpDriver","retrieved":"2020-10-25T06:39:09.486Z","duration":"117.3589ms"}}"""
        val expectedResponse = serializer.parse(IdentifierResponse.serializer(), expectedResponseString)
        val expectedWellKnownConfigDocumentResponse = """{"@context":"https://identity.foundation/.well-known/contexts/did-configuration-v0.0.jsonld","linked_dids":["eyJhbGciOiJFUzI1NksiLCJraWQiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkjc2lnX2VkMmM1ZWRmIn0.eyJzdWIiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJpc3MiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJuYmYiOjE2MDM0MTU2NjQsImV4cCI6MjM5MjMzNDA2NCwidmMiOnsiQGNvbnRleHQiOlsiaHR0cHM6Ly93d3cudzMub3JnLzIwMTgvY3JlZGVudGlhbHMvdjEiLCJodHRwczovL2lkZW50aXR5LmZvdW5kYXRpb24vLndlbGwta25vd24vY29udGV4dHMvZGlkLWNvbmZpZ3VyYXRpb24tdjAuMC5qc29ubGQiXSwiaXNzdWVyIjoiZGlkOmlvbjpFaUE4SFIyOG01S1VpZzllbFBSWGttS3Z2QkdYY09veHBVckNzY1RkR0pjSVhRPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9ZWE5vSWpvaVJXbERWRFY1TUc1bk5USkNOelZqWVdGcVdVOXFWakJSTW1weFNuZzBORFpTYWpoUlRqRnBhSGR0ZVVwSlp5SXNJbkpsWTI5MlpYSjVYMk52YlcxcGRHMWxiblFpT2lKRmFVUnNPRVI0ZVhaTGEzbHZSbUpzVVdwME9YbGxVMkozVFhrd1IwODNNRk0xUjJGVVUxRjBVbEYwYUZKUkluMC5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFJHRlhTMnN5Y2pKaVNXSnNSV0ZwUlVodFJVNUtjMmg2Y3podFkxaEpkMmhUVjBaMVltdFdRbEozV1djaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ3SW1sa0lqb2ljMmxuWDJWa01tTTFaV1JtSWl3aWRIbHdaU0k2SWtWalpITmhVMlZqY0RJMU5tc3hWbVZ5YVdacFkyRjBhVzl1UzJWNU1qQXhPU0lzSW1wM2F5STZleUpyZEhraU9pSkZReUlzSW1OeWRpSTZJbk5sWTNBeU5UWnJNU0lzSW5naU9pSm9UemhZYVhodFdVeE5PVlZWTW1GV09XOWtjMlZzU0ROb2JESnRiVkZQTFMxR1R6TkthMkpyZWtWcklpd2llU0k2SW5CRFdFcHhiWHBVYnpWUVFrZFJURVJpYm5SdGRVRmFTRWxaUW5GWk9HMURaVmRrYVdocGIwdEdVbU1pZlN3aWNIVnljRzl6WlNJNld5SmhkWFJvSWl3aVoyVnVaWEpoYkNKZGZWMTlmVjE5IiwiaXNzdWFuY2VEYXRlIjoiMjAyMC0xMC0yM1QwMToxNDoyNC43NzRaIiwiZXhwaXJhdGlvbkRhdGUiOiIyMDQ1LTEwLTIzVDAxOjE0OjI0Ljc3NFoiLCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiRG9tYWluTGlua2FnZUNyZWRlbnRpYWwiXSwiY3JlZGVudGlhbFN1YmplY3QiOnsiaWQiOiJkaWQ6aW9uOkVpQThIUjI4bTVLVWlnOWVsUFJYa21LdnZCR1hjT294cFVyQ3NjVGRHSmNJWFE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFZEVjVNRzVuTlRKQ056VmpZV0ZxV1U5cVZqQlJNbXB4U25nME5EWlNhamhSVGpGcGFIZHRlVXBKWnlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSc09FUjRlWFpMYTNsdlJtSnNVV3AwT1hsbFUySjNUWGt3UjA4M01GTTFSMkZVVTFGMFVsRjBhRkpSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdGWFMyc3ljakppU1dKc1JXRnBSVWh0UlU1S2MyaDZjemh0WTFoSmQyaFRWMFoxWW10V1FsSjNXV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMlZrTW1NMVpXUm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1R6aFlhWGh0V1V4Tk9WVlZNbUZXT1c5a2MyVnNTRE5vYkRKdGJWRlBMUzFHVHpOS2EySnJla1ZySWl3aWVTSTZJbkJEV0VweGJYcFVielZRUWtkUlRFUmliblJ0ZFVGYVNFbFpRbkZaT0cxRFpWZGthV2hwYjB0R1VtTWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJvcmlnaW4iOiJodHRwczovL2lzc3VlcnRlc3RuZy5jb20ifX19.luP4vIE30u_kj5CINDhbjZcsHDbEEXS5hMFnzd7qVKs2OtseDJlYcgLOgbCqI17v3edXx9OVkErQ-sSuIGvt8g"]}"""
        val expectedWellKnownConfigDocument = serializer.parse(LinkedDomainsResponse.serializer(), expectedWellKnownConfigDocumentResponse)
        val expectedDomainUrl = "https://issuertestng.com"
        coEvery { mockedResolver.resolve(suppliedDidWithMultipleServiceEndpoints) } returns Result.Success(expectedResponse.didDocument)
        coEvery { linkedDomainsService["getWellKnownConfigDocument"](expectedDomainUrl) } returns Result.Success(expectedWellKnownConfigDocument)
        coEvery { mockedJwtValidator.verifySignature(any()) } returns true
        runBlocking {
            val linkedDomainsArrResult = linkedDomainsService.fetchAndVerifyLinkedDomains(suppliedDidWithMultipleServiceEndpoints)
            assertThat(linkedDomainsArrResult).isInstanceOf(Result.Success::class.java)
            assertThat((linkedDomainsArrResult as Result.Success).payload).isInstanceOf(LinkedDomainVerified::class.java)
            assertThat((linkedDomainsArrResult.payload as LinkedDomainVerified).domainUrl).isEqualTo(expectedDomainUrl)
        }
    }

    @Test
    fun `test did without service endpoints`() {
        val suppliedDidWithoutServiceEndpoint =
            "did:ion:EiAGYVovJcSCiUWuX9K1eFHBcv4BorIjMG7e44hf1hKtGg?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDUDBKOEVyRmZXeEw2WGNqT2g4STU2Smp3bXhVQ01zWk5yT2ZoSWFMbUxVQSIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaUQyNHVWbkd1ZW9aZUs0OEl1aE9BZ1c4Z3NvTmdncHV2bGRRSUVjM09wNFZRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXpYZmprQkE1Z05tZWJOam56TmhkYzYycjdCUkJremcyOXFLWVBON3MtQUEiLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoiY2FwcHRvc28taXNzdWVyLXNpdGUtc2lnbmluZy1rZXkiLCJ0eXBlIjoiRWNkc2FTZWNwMjU2azFWZXJpZmljYXRpb25LZXkyMDE5IiwiandrIjp7ImtpZCI6Imh0dHBzOi8vdmMtMjAyMC1rdi52YXVsdC5henVyZS5uZXQva2V5cy9jYXBwdG9zby1pc3N1ZXItc2l0ZS1zaWduaW5nLWtleS9lZTM5MDUxNGFhN2Y0ZjNiYTAzZjViNDM3ZjNlYjRlZSIsImt0eSI6IkVDIiwiY3J2Ijoic2VjcDI1NmsxIiwieCI6IkFIQ29XM1k4cHVvRmFqa0JqeU1HcUtwZTJ3TktFb1BaSWtINDVxelJaeVUiLCJ5IjoiQ3JaaU02VU1sLVFNMnlCYTgtaS1kTlM3X1JyeDA3VnN3OVlTVlA4UzBxTSJ9LCJwdXJwb3NlIjpbImF1dGgiLCJnZW5lcmFsIl19XX19XX0"
        val actualDidDocString =
            """{"@context":"https://www.w3.org/ns/did-resolution/v1","didDocument":{"id":"did:ion:EiAGYVovJcSCiUWuX9K1eFHBcv4BorIjMG7e44hf1hKtGg?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDUDBKOEVyRmZXeEw2WGNqT2g4STU2Smp3bXhVQ01zWk5yT2ZoSWFMbUxVQSIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaUQyNHVWbkd1ZW9aZUs0OEl1aE9BZ1c4Z3NvTmdncHV2bGRRSUVjM09wNFZRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXpYZmprQkE1Z05tZWJOam56TmhkYzYycjdCUkJremcyOXFLWVBON3MtQUEiLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoiY2FwcHRvc28taXNzdWVyLXNpdGUtc2lnbmluZy1rZXkiLCJ0eXBlIjoiRWNkc2FTZWNwMjU2azFWZXJpZmljYXRpb25LZXkyMDE5IiwiandrIjp7ImtpZCI6Imh0dHBzOi8vdmMtMjAyMC1rdi52YXVsdC5henVyZS5uZXQva2V5cy9jYXBwdG9zby1pc3N1ZXItc2l0ZS1zaWduaW5nLWtleS9lZTM5MDUxNGFhN2Y0ZjNiYTAzZjViNDM3ZjNlYjRlZSIsImt0eSI6IkVDIiwiY3J2Ijoic2VjcDI1NmsxIiwieCI6IkFIQ29XM1k4cHVvRmFqa0JqeU1HcUtwZTJ3TktFb1BaSWtINDVxelJaeVUiLCJ5IjoiQ3JaaU02VU1sLVFNMnlCYTgtaS1kTlM3X1JyeDA3VnN3OVlTVlA4UzBxTSJ9LCJwdXJwb3NlIjpbImF1dGgiLCJnZW5lcmFsIl19XX19XX0","@context":["https://www.w3.org/ns/did/v1",{"@base":"did:ion:EiAGYVovJcSCiUWuX9K1eFHBcv4BorIjMG7e44hf1hKtGg?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlDUDBKOEVyRmZXeEw2WGNqT2g4STU2Smp3bXhVQ01zWk5yT2ZoSWFMbUxVQSIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaUQyNHVWbkd1ZW9aZUs0OEl1aE9BZ1c4Z3NvTmdncHV2bGRRSUVjM09wNFZRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQXpYZmprQkE1Z05tZWJOam56TmhkYzYycjdCUkJremcyOXFLWVBON3MtQUEiLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoiY2FwcHRvc28taXNzdWVyLXNpdGUtc2lnbmluZy1rZXkiLCJ0eXBlIjoiRWNkc2FTZWNwMjU2azFWZXJpZmljYXRpb25LZXkyMDE5IiwiandrIjp7ImtpZCI6Imh0dHBzOi8vdmMtMjAyMC1rdi52YXVsdC5henVyZS5uZXQva2V5cy9jYXBwdG9zby1pc3N1ZXItc2l0ZS1zaWduaW5nLWtleS9lZTM5MDUxNGFhN2Y0ZjNiYTAzZjViNDM3ZjNlYjRlZSIsImt0eSI6IkVDIiwiY3J2Ijoic2VjcDI1NmsxIiwieCI6IkFIQ29XM1k4cHVvRmFqa0JqeU1HcUtwZTJ3TktFb1BaSWtINDVxelJaeVUiLCJ5IjoiQ3JaaU02VU1sLVFNMnlCYTgtaS1kTlM3X1JyeDA3VnN3OVlTVlA4UzBxTSJ9LCJwdXJwb3NlIjpbImF1dGgiLCJnZW5lcmFsIl19XX19XX0"}],"publicKey":[{"id":"#capptoso-issuer-site-signing-key","controller":"","type":"EcdsaSecp256k1VerificationKey2019","publicKeyJwk":{"kid":"https://vc-2020-kv.vault.azure.net/keys/capptoso-issuer-site-signing-key/ee390514aa7f4f3ba03f5b437f3eb4ee","kty":"EC","crv":"secp256k1","x":"AHCoW3Y8puoFajkBjyMGqKpe2wNKEoPZIkH45qzRZyU","y":"CrZiM6UMl-QM2yBa8-i-dNS7_Rrx07Vsw9YSVP8S0qM"}}],"authentication":["#capptoso-issuer-site-signing-key"]},"methodMetadata":{"published":false,"recoveryCommitment":"EiD24uVnGueoZeK48IuhOAgW8gsoNggpuvldQIEc3Op4VQ","updateCommitment":"EiAzXfjkBA5gNmebNjnzNhdc62r7BRBkzg29qKYPN7s-AA"},"resolverMetadata":{"driverId":"did:ion","driver":"HttpDriver","retrieved":"2020-10-21T20:03:42.743Z","duration":"115.0629ms"}}"""
        val actualDidDoc = serializer.parse(IdentifierResponse.serializer(), actualDidDocString)
        coEvery { mockedResolver.resolve(suppliedDidWithoutServiceEndpoint) } returns Result.Success(actualDidDoc.didDocument)
        runBlocking {
            val actualDomainUrlResult = linkedDomainsService.fetchAndVerifyLinkedDomains(suppliedDidWithoutServiceEndpoint)
            assertThat(actualDomainUrlResult).isInstanceOf(Result.Success::class.java)
            assertThat((actualDomainUrlResult as Result.Success).payload).isInstanceOf(LinkedDomainUnVerified::class.java)
            assertThat((actualDomainUrlResult.payload as LinkedDomainUnVerified).domainUrl).isEqualTo("")
        }
    }
}