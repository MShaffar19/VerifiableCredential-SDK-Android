/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import IdentifierDocument from '../src/IdentifierDocument';
import Identifier from '../src/Identifier';
import UserAgentOptions from '../src/UserAgentOptions';
import TestResolver from './TestResolver';
import UserAgentError from '../src/UserAgentError';
import { KeyUse, KeyType } from '@decentralized-identity/did-common-typescript';

describe('Identifier', () => {
  const testResolver = new TestResolver();

  // Configure the agent options for the tests
  const options = {
    resolver: testResolver,
    timeoutInSeconds: 30
  } as UserAgentOptions;

  it('should construct a storage identfier for the key', () => {
    let identifier = Identifier.keyStorageIdentifier('did:ion:identifier', 'peer', KeyUse.Encryption, KeyType.EC);
    expect(identifier).toEqual('did:ion:identifier-peer-enc-EC');
    identifier = Identifier.keyStorageIdentifier('did:ion:identifier', 'peer', KeyUse.Encryption, KeyType.RSA);
    expect(identifier).toEqual('did:ion:identifier-peer-enc-RSA');
    identifier = Identifier.keyStorageIdentifier('did:ion:identifier', 'peer', KeyUse.Signature, KeyType.EC);
    expect(identifier).toEqual('did:ion:identifier-peer-sig-EC');
    identifier = Identifier.keyStorageIdentifier('did:ion:identifier', 'peer', KeyUse.Signature, KeyType.RSA);
    expect(identifier).toEqual('did:ion:identifier-peer-sig-RSA');
  });

  it('should construct new instance when provided an identifier string', () => {
    const identifier = new Identifier('did:ion:identifier', options);
    expect(identifier).toBeDefined();
    expect(identifier.id).toEqual('did:ion:identifier');
  });

  it('should construct new instance when provided an identifier document', () => {
    const identifierDocument = new IdentifierDocument(
      { id: 'did:ion:identifier', created: '2019-01-25T01:08:44.732Z' }
    );
    const identifier = new Identifier(identifierDocument, options);
    expect(identifier).toBeDefined();
    expect(identifier.id).toEqual('did:ion:identifier');
  });

  it('should return document from local', () => {
    const identifierDocument = new IdentifierDocument(
      { id: 'did:ion:identifier', created: '2019-01-25T01:08:44.732Z' }
    );
    const identifier = new Identifier(identifierDocument, options);

    const resolver = spyOn<any>(testResolver, 'resolve').and.callThrough();
    const localDocument = identifier.getDocument();
    expect(localDocument).toBeDefined();
    expect(resolver).not.toHaveBeenCalled();
  });

  it('should throw when no resolver specified and document not cached', async done => {
    const options = {
      timeoutInSeconds: 30
    } as UserAgentOptions;

    const identifier = new Identifier('did:ion:identifier', options);
    await identifier
      .getDocument()
      .catch(error => {
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual('Resolver not specified in user agent options.');
      })
      .finally(done);
  });

  it('should resolve identifier and return document', () => {
    const identifier = new Identifier('did:ion:identifier', options);
    const identifierDocument = new IdentifierDocument(
      { id: 'did:ion:identifier', created: '2019-01-25T01:08:44.732Z' }
    );
    (options.resolver as TestResolver).prepareTest(identifier, identifierDocument);

    const resolver = spyOn(testResolver, 'resolve').and.callThrough();
    const result = identifier.getDocument();
    expect(result).toBeDefined();
    expect(resolver).toHaveBeenCalled();
  });

  it('should call getDocument() when local document undefined', async done => {
    const identifier = new Identifier('did:ion:identifier', options);
    const identifierDocument = new IdentifierDocument(
      {
        id: 'did:ion:identifier',
        created: '2019-01-25T01:08:44.732Z',
        publicKeys: [
          {
            id: '#master',
            type: 'RsaVerificationKey2018',
            publicKeyJwk: {
              kty: 'RSA',
              kid: '#master',
              keyOps: [
                'sign',
                'verify'
              ],
              n: 'vdpHn7kNq42UMC1W8bwxgE7K...',
              e: 'AQAB'
            }
          }
        ]
      }
    );

    (options.resolver as TestResolver).prepareTest(identifier, identifierDocument);

    const resolver = spyOn(testResolver, 'resolve').and.callThrough();
    const result = await identifier.getPublicKey('#master');
    expect(result).toBeDefined();
    expect(resolver).toHaveBeenCalled();
    done();
  });

  it('should throw when document has no keys', async done => {
    const identifier = new Identifier('did:ion:identifier', options);
    const identifierDocument = new IdentifierDocument(
      {
        id: 'did:ion:identifier',
        created: '2019-01-25T01:08:44.732Z'
      }
    );

    (options.resolver as TestResolver).prepareTest(identifier, identifierDocument);
    await identifier
      .getPublicKey('#master')
      .catch(error => {
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual('Document does not contain any public keys');
      })
      .finally(done);
  });

  it('should throw when document does not contain specified key', async done => {
    const identifier = new Identifier('did:ion:identifier', options);
    const identifierDocument = new IdentifierDocument(
      {
        id: 'did:ion:identifier',
        created: '2019-01-25T01:08:44.732Z',
        publicKeys: [
          {
            id: '#master',
            type: 'RsaVerificationKey2018',
            publicKeyJwk: {
              kty: 'RSA',
              kid: '#master',
              keyOps: [
                'sign',
                'verify'
              ],
              n: 'vdpHn7kNq42UMC1W8bwxgE7K...',
              e: 'AQAB'
            }
          }
        ]
      }
    );

    (options.resolver as TestResolver).prepareTest(identifier, identifierDocument);
    await identifier
      .getPublicKey('#notInDocument')
      .catch(error => {
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual(`Document does not contain a key with id '#notInDocument'`);
      })
      .finally(done);
  });

  it('should return public key for specified key identifier', async done => {
    const identifier = new Identifier('did:ion:identifier', options);
    const identifierDocument = new IdentifierDocument(
      {
        id: 'did:ion:identifier',
        created: '2019-01-25T01:08:44.732Z',
        publicKeys: [
          {
            id: '#master',
            type: 'RsaVerificationKey2018',
            publicKeyJwk: {
              kty: 'RSA',
              kid: '#master',
              keyOps: [
                'sign',
                'verify'
              ],
              n: 'vdpHn7kNq42UMC1W8bwxgE7K...',
              e: 'AQAB'
            }
          }
        ]
      }
    );

    (options.resolver as TestResolver).prepareTest(identifier, identifierDocument);
    const publicKey = await identifier.getPublicKey('#master');
    expect(publicKey).toBeDefined();
    expect(publicKey.id).toEqual('#master');
    done();
  });

  it('should return first public key when no key identifier specified', async done => {
    const identifier = new Identifier('did:ion:identifier', options);
    const identifierDocument = new IdentifierDocument(
      {
        id: 'did:ion:identifier',
        created: '2019-01-25T01:08:44.732Z',
        publicKeys: [
          {
            id: '#first',
            type: 'RsaVerificationKey2018',
            publicKeyJwk: {
              kty: 'RSA',
              kid: '#master',
              keyOps: [
                'sign',
                'verify'
              ],
              n: 'vdpHn7kNq42UMC1W8bwxgE7K...',
              e: 'AQAB'
            }
          },
          {
            id: '#second',
            type: 'RsaVerificationKey2018',
            publicKeyJwk: {
              kty: 'RSA',
              kid: '#master',
              keyOps: [
                'sign',
                'verify'
              ],
              n: 'vdpHn7kNq42UMC1W8bwxgE7K...',
              e: 'AQAB'
            }
          }
        ]
      }
    );

    (options.resolver as TestResolver).prepareTest(identifier, identifierDocument);
    const publicKey = await identifier.getPublicKey();
    expect(publicKey).toBeDefined();
    expect(publicKey.id).toEqual('#first');
    done();
  });
});
