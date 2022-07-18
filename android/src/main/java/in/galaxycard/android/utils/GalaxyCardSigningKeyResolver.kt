package `in`.galaxycard.android.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SigningKeyResolverAdapter
import java.security.Key
import javax.crypto.spec.SecretKeySpec


class GalaxyCardSigningKeyResolver(private val keyBytes: ByteArray): SigningKeyResolverAdapter() {
    override fun resolveSigningKey(jwsHeader: JwsHeader<*>?, claims: Claims?): Key {
        return SecretKeySpec(keyBytes, SignatureAlgorithm.forName(jwsHeader?.algorithm).jcaName)
    }
}