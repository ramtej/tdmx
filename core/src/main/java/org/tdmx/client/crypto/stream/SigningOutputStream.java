/**
 * 
 */
package org.tdmx.client.crypto.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.converters.NumberToOctetString;
import org.tdmx.client.crypto.scheme.CryptoException;

/**
 * SigningOutputStream can count the size and create a DigitalSignature value on close.
 * 
 * The size counts the number of bytes written to the delegate output stream, excl. the 
 * signature value written optionally on close to the output stream.
 * 
 * The signature value and size available after {@link #close()} is called.
 * 
 * If {@link #signLengthOnClose} is true, then the length of written bytes is appended to
 * the digital signature on close ( before optionally writing the signature to the output stream ).
 * 
 * If {@link #outputSignatureOnClose} is true, then the signature value is written to 
 * the output stream prior to closing the output stream.
 * 
 * @author Peter
 *
 */
public class SigningOutputStream extends OutputStream {

	private OutputStream output;
	private Signature signature;
	private boolean signLengthOnClose = false;
	private boolean outputSignatureOnClose = false;

	private byte[] signatureValue = null;
	private long size = 0;
	
	public SigningOutputStream( SignatureAlgorithm signatureAlgorithm, PrivateKey privateKey, boolean outputSignatureOnClose, boolean signLengthOnClose, OutputStream output ) throws CryptoException {
		this.signature = signatureAlgorithm.getSignature(privateKey);
		this.outputSignatureOnClose = outputSignatureOnClose;
		this.signLengthOnClose = signLengthOnClose;
		this.output = output;
	}

	@Override
	public void write(int b) throws IOException {
		output.write(b);
		size++;
		try {
			signature.update((byte)b);
		} catch (SignatureException e) {
			throw new IOException("Unable to update Signature.", e);
		}
	}

	@Override
    public void write(byte b[]) throws IOException {
        output.write(b, 0, b.length);
        size+=b.length;
        try {
        	signature.update(b);
		} catch (SignatureException e) {
			throw new IOException("Unable to update Signature.", e);
		}
    }
	
	@Override
    public void write(byte b[], int off, int len) throws IOException {
		output.write(b, off, len);
		size+=len;
		try {
			signature.update(b, off, len);
		} catch (SignatureException e) {
			throw new IOException("Unable to update Signature.", e);
		}
    }
	
	@Override
    public void flush() throws IOException {
		output.flush();
    }

	@Override
    public void close() throws IOException {
		if ( signLengthOnClose ) {
			byte[] length = NumberToOctetString.longToBytes(size);
			try {
				signature.update(length);
			} catch (SignatureException e) {
				throw new IOException("Unable to add length to Signature.", e);
			}
		}
		try {
			signatureValue = signature.sign();
		} catch (SignatureException e) {
			throw new IOException("Unable to sign Signature.", e);
		}
		if ( outputSignatureOnClose ) {
			output.write(signatureValue); // !do not increase the size anymore
			output.flush();
		}
		output.close();
    }

	/**
	 * @return the signatureValue
	 */
	public byte[] getSignatureValue() {
		return signatureValue;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

}
