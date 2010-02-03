// Copyright (c) Corporation for National Research Initiatives
package org.python.core;

import java.io.Serializable;
import java.math.BigInteger;

import org.python.expose.ExposedMethod;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedType;
import org.python.expose.MethodType;

/**
 * A builtin python int.
 */
@ExposedType(name = "int", doc = BuiltinDocs.int_doc)
public class PyInteger extends PyObject {
    
    public static final PyType TYPE = PyType.fromClass(PyInteger.class);

    /** The minimum value of an int represented by a BigInteger */
    public static final BigInteger minInt = BigInteger.valueOf(Integer.MIN_VALUE);

    /** The maximum value of an int represented by a BigInteger */
    public static final BigInteger maxInt = BigInteger.valueOf(Integer.MAX_VALUE);

    @ExposedNew
    public static PyObject int_new(PyNewWrapper new_, boolean init, PyType subtype,
            PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("int", args, keywords, new String[] { "x",
                "base" }, 0);
        PyObject x = ap.getPyObject(0, null);
        int base = ap.getInt(1, -909);
        if (new_.for_type == subtype) {
            if (x == null) {
                return Py.Zero;
            }
			if (base == -909) {
            	if (x instanceof PyBoolean) {
            		return (coerce(x) == 0) ? Py.Zero : Py.One;
            	}
				return asPyInteger(x);
			}
			if (!(x instanceof PyString)) {
				throw Py.TypeError("int: can't convert non-string with explicit base");
			}
            try {
                return Py.newInteger(((PyString)x).atoi(base));
            } catch (PyException pye) {
                if (pye.match(Py.OverflowError)) {
                    return ((PyString)x).atol(base);
                }
                throw pye;
            }
        } else {
            if (x == null) {
                return new PyIntegerDerived(subtype, 0);
            }
            if (base == -909) {
				PyObject intOrLong = asPyInteger(x);
				if (intOrLong instanceof PyInteger) {
					return new PyIntegerDerived(subtype, ((PyInteger) intOrLong).getValue());
				} else {
					throw Py.OverflowError("long int too large to convert to int");
				}
            }
            if (!(x instanceof PyString)) {
                throw Py
                        .TypeError("int: can't convert non-string with explicit base");
            }
            return new PyIntegerDerived(subtype, ((PyString) x).atoi(base));
        }
    } // xxx

    /**
     * @return the result of x.__int__ 
     * @throws Py.Type error if x.__int__ throws an Py.AttributeError
     */
	private static PyObject asPyInteger(PyObject x) {
		try {
			return x.__int__();
		} catch (PyException pye) {
			if (!pye.match(Py.AttributeError))
				throw pye;
			throw Py.TypeError("int() argument must be a string or a number");
		}
	}
    
    private int value;

    public PyInteger(PyType subType, int v) {
        super(subType);
        value = v;
    }

    public PyInteger(int v) {
        this(TYPE, v);
    }

    public int getValue() {
        return value;
    }

    public String toString() {
        return int_toString();
    }

    //XXX: need separate __doc__ for __repr__
    @ExposedMethod(names = {"__str__", "__repr__"}, doc = BuiltinDocs.int___str___doc)
    final String int_toString() {
        return Integer.toString(getValue());
    }

    public int hashCode() {
        return int_hashCode();
    }

    @ExposedMethod(names = "__hash__", doc = BuiltinDocs.int___hash___doc)
    final int int_hashCode() {
        return getValue();
    }

    public boolean __nonzero__() {
        return int___nonzero__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___nonzero___doc)
    final boolean int___nonzero__() {
        return getValue() != 0;
    }

    public Object __tojava__(Class<?> c) {
        if (c == Integer.TYPE || c == Number.class ||
            c == Object.class || c == Integer.class ||
            c == Serializable.class)
        {
            return new Integer(getValue());
        }

        if (c == Boolean.TYPE || c == Boolean.class)
            return new Boolean(getValue() != 0);
        if (c == Byte.TYPE || c == Byte.class)
            return new Byte((byte)getValue());
        if (c == Short.TYPE || c == Short.class)
            return new Short((short)getValue());

        if (c == Long.TYPE || c == Long.class)
            return new Long(getValue());
        if (c == Float.TYPE || c == Float.class)
            return new Float(getValue());
        if (c == Double.TYPE || c == Double.class)
            return new Double(getValue());
        return super.__tojava__(c);
    }

    public int __cmp__(PyObject other) {
        return int___cmp__(other);
    }

    @ExposedMethod(type = MethodType.CMP, doc = BuiltinDocs.int___cmp___doc)
    final int int___cmp__(PyObject other) {
        if (!canCoerce(other))
             return -2;
        int v = coerce(other);
        return getValue() < v ? -1 : getValue() > v ? 1 : 0;
    }

    public Object __coerce_ex__(PyObject other) {
        return int___coerce_ex__(other);
    }

    @ExposedMethod(doc = BuiltinDocs.int___coerce___doc)
    final PyObject int___coerce__(PyObject other) {
        return adaptToCoerceTuple(int___coerce_ex__(other));
    }

    /** 
     * Coercion logic for int. Implemented as a final method to avoid
     * invocation of virtual methods from the exposed coerced. 
     */     
    final Object int___coerce_ex__(PyObject other) {
        if (other instanceof PyInteger)
            return other;
        else
            return Py.None;
    }

    private static final boolean canCoerce(PyObject other) {
        return other instanceof PyInteger;
    }

    private static final int coerce(PyObject other) {
        if (other instanceof PyInteger)
            return ((PyInteger) other).getValue();
        else
            throw Py.TypeError("xxx");
    }


    public PyObject __add__(PyObject right) {
        return int___add__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___add___doc)
    final PyObject int___add__(PyObject right) {
        if (!canCoerce(right))
            return null;
        int rightv = coerce(right);
        int a = getValue();
        int b = rightv;
        int x = a + b;
        if ((x^a) >= 0 || (x^b) >= 0)
            return Py.newInteger(x);
        return new PyLong((long) a + (long)b);
    }

    public PyObject __radd__(PyObject left) {
        return int___radd__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___radd___doc)
    final PyObject int___radd__(PyObject left) {
        return __add__(left);
    }

    private static PyObject _sub(int a, int b) {
        int x = a - b;
        if ((x^a) >= 0 || (x^~b) >= 0)
            return Py.newInteger(x);
        return new PyLong((long) a - (long)b);
    }

    public PyObject __sub__(PyObject right) {
        return int___sub__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___sub___doc)
    final PyObject int___sub__(PyObject right) {
        if (!canCoerce(right))
            return null;
        return _sub(getValue(), coerce(right));
    }

    public PyObject __rsub__(PyObject left) {
        return int___rsub__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rsub___doc)
    final PyObject int___rsub__(PyObject left) {
        if (!canCoerce(left))
            return null;
        return _sub(coerce(left), getValue());
    }

    public PyObject __mul__(PyObject right) {
        return int___mul__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___mul___doc)
    final PyObject int___mul__(PyObject right) {
        if (!canCoerce(right))
            return null;
        int rightv = coerce(right);

        double x = getValue();
        x *= rightv;
        //long x = ((long)getValue())*((PyInteger)right).getValue();
        //System.out.println("mul: "+this+" * "+right+" = "+x);

        if (x <= Integer.MAX_VALUE && x >= Integer.MIN_VALUE)
            return Py.newInteger((int)x);
        return __long__().__mul__(right);
    }

    public PyObject __rmul__(PyObject left) {
        return int___rmul__(left);
    }
    
    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rmul___doc)
    final PyObject int___rmul__(PyObject left) {
        return __mul__(left);
    }

    // Getting signs correct for integer division
    // This convention makes sense when you consider it in tandem with modulo
    private static long divide(long x, long y) {
        if (y == 0) {
            throw Py.ZeroDivisionError("integer division or modulo by zero");
        }
        long xdivy = x / y;
        long xmody = x - xdivy * y;
        
        /* If the signs of x and y differ, and the remainder is non-0,
         * C89 doesn't define whether xdivy is now the floor or the
         * ceiling of the infinitely precise quotient.  We want the floor,
         * and we have it iff the remainder's sign matches y's.
         */
        
        if (xmody != 0 && ((y < 0 && xmody > 0) || (y > 0 && xmody < 0))) {
            xmody += y;
            --xdivy;
            //assert(xmody && ((y ^ xmody) >= 0));
        }
        return xdivy;
    }

    @Override
    public PyObject __div__(PyObject right) {
        return int___div__(right);
    }
    
    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___div___doc)
    final PyObject int___div__(PyObject right) {
        if (!canCoerce(right))
            return null;
        if (Options.divisionWarning > 0)
            Py.warning(Py.DeprecationWarning, "classic int division");
        return Py.newInteger(divide(getValue(), coerce(right)));
    }

    public PyObject __rdiv__(PyObject left) {
        return int___rdiv__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rdiv___doc)
    final PyObject int___rdiv__(PyObject left) {
        if (!canCoerce(left))
            return null;
        if (Options.divisionWarning > 0)
            Py.warning(Py.DeprecationWarning, "classic int division");
        return Py.newInteger(divide(coerce(left), getValue()));
    }

    public PyObject __floordiv__(PyObject right) {
        return int___floordiv__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___floordiv___doc)
    final PyObject int___floordiv__(PyObject right) {
        if (!canCoerce(right))
            return null;
        return Py.newInteger(divide(getValue(), coerce(right)));
    }

    public PyObject __rfloordiv__(PyObject left) {
        return int___rfloordiv__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rfloordiv___doc)
    final PyObject int___rfloordiv__(PyObject left) {
        if (!canCoerce(left))
            return null;
        return Py.newInteger(divide(coerce(left), getValue()));
    }

    public PyObject __truediv__(PyObject right) {
        return int___truediv__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___truediv___doc)
    final PyObject int___truediv__(PyObject right) {
        if (right instanceof PyInteger)
            return __float__().__truediv__(right);
        else if(right instanceof PyLong)
            return int___long__().__truediv__(right);
	else
            return null;
    }

    public PyObject __rtruediv__(PyObject left) {
        return int___rtruediv__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rtruediv___doc)
    final PyObject int___rtruediv__(PyObject left) {
        if (left instanceof PyInteger)
            return left.__float__().__truediv__(this);
        else if(left instanceof PyLong)
            return left.__truediv__(int___long__());
        else
            return null;
    }

    private static long modulo(long x, long y, long xdivy) {
        return x - xdivy*y;
    }

    public PyObject __mod__(PyObject right) {
        return int___mod__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___mod___doc)
    final PyObject int___mod__(PyObject right) {
        if (!canCoerce(right))
            return null;
        int rightv = coerce(right);
        int v = getValue();
        return Py.newInteger(modulo(v, rightv, divide(v, rightv)));
    }

    public PyObject __rmod__(PyObject left) {
        return int___rmod__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rmod___doc)
    final PyObject int___rmod__(PyObject left) {
        if (!canCoerce(left))
            return null;
        int leftv = coerce(left);
        int v = getValue();
        return Py.newInteger(modulo(leftv, v, divide(leftv, v)));
    }

    public PyObject __divmod__(PyObject right) {
        return int___divmod__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___divmod___doc)
    final PyObject int___divmod__(PyObject right) {
        if (!canCoerce(right))
            return null;
        int rightv = coerce(right);

        int v = getValue();
        long xdivy = divide(v, rightv);
        return new PyTuple(Py.newInteger(xdivy), Py.newInteger(modulo(v, rightv, xdivy)));
    }
    
    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rdivmod___doc)
    final PyObject int___rdivmod__(PyObject left){
        if (!canCoerce(left))
            return null;
        int leftv = coerce(left);

        int v = getValue();
        long xdivy = divide(leftv, v);
        return new PyTuple(Py.newInteger(xdivy), Py.newInteger(modulo(leftv, v, xdivy)));
    }

    public PyObject __pow__(PyObject right, PyObject modulo) {
        return int___pow__(right,modulo);
    }

    @ExposedMethod(type = MethodType.BINARY, defaults = {"null"}, doc = BuiltinDocs.int___pow___doc)
    final PyObject int___pow__(PyObject right, PyObject modulo) {
        if (!canCoerce(right))
            return null;

        if (modulo != null && !canCoerce(modulo))
            return null;

        return _pow(getValue(), coerce(right), modulo, this, right);
    }

    public PyObject __rpow__(PyObject left, PyObject modulo) {
        if (!canCoerce(left))
            return null;

        if (modulo != null && !canCoerce(modulo))
            return null;

        return _pow(coerce(left), getValue(), modulo, left, this);
    }
    
    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rpow___doc)
    final PyObject int___rpow__(PyObject left){
    	return __rpow__(left, null);
    }

    private static PyObject _pow(int value, int pow, PyObject modulo, PyObject left, PyObject right) {
        int mod = 0;
        long tmp = value;
        boolean neg = false;
        if (tmp < 0) {
            tmp = -tmp;
            neg = (pow & 0x1) != 0;
        }
        long result = 1;

        if (pow < 0) {
            if (value != 0)
                return left.__float__().__pow__(right, modulo);
            else
                throw Py.ZeroDivisionError("cannot raise 0 to a " +
                                           "negative power");
        }

        if (modulo != null) {
            mod = coerce(modulo);
            if (mod == 0) {
                throw Py.ValueError("pow(x, y, z) with z==0");
            }
        }

        // Standard O(ln(N)) exponentiation code
        while (pow > 0) {
            if ((pow & 0x1) != 0) {
                result *= tmp;
                if (mod != 0) {
                    result %= mod;
                }

                if (result > Integer.MAX_VALUE) {
                    return left.__long__().__pow__(right, modulo);
                }
            }
            pow >>= 1;
            if (pow == 0)
                break;
            tmp *= tmp;

            if (mod != 0) {
                tmp %= mod;
            }

            if (tmp > Integer.MAX_VALUE) {
                return left.__long__().__pow__(right, modulo);
            }
        }

        if (neg)
            result = -result;

        // Cleanup result of modulo
        if (mod != 0) {
            result = modulo(result, mod, divide(result, mod));
        }
        return Py.newInteger(result);
    }

    public PyObject __lshift__(PyObject right) {
        return int___lshift__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___lshift___doc)
    final PyObject int___lshift__(PyObject right) {
		int rightv;
		if (right instanceof PyInteger)
			rightv = ((PyInteger) right).getValue();
		else if (right instanceof PyLong)
			return int___long__().__lshift__(right);
		else
			return null;

        if (rightv >= Integer.SIZE) {
            return __long__().__lshift__(right);
        } else if (rightv < 0) {
            throw Py.ValueError("negative shift count");
        }
        int result = getValue() << rightv;
        if (getValue() != result >> rightv) {
            return __long__().__lshift__(right);
        }
        return Py.newInteger(result);
    }
    
    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rlshift___doc)
    final PyObject int___rlshift__(PyObject left) {
		int leftv;
		if (left instanceof PyInteger)
			leftv = ((PyInteger) left).getValue();
		else if (left instanceof PyLong)
			return left.__rlshift__(int___long__());
		else
			return null;

        if (getValue() >= Integer.SIZE) {
            return left.__long__().__lshift__(this);
        } else if (getValue() < 0) {
            throw Py.ValueError("negative shift count");
        }
        int result = leftv << getValue();
        if (leftv != result >> getValue()) {
            return left.__long__().__lshift__(this);
        }
        return Py.newInteger(result);
    }

    public PyObject __rshift__(PyObject right) {
        return int___rshift__(right);
    }
    
    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rshift___doc)
	final PyObject int___rshift__(PyObject right) {
		int rightv;
		if (right instanceof PyInteger)
			rightv = ((PyInteger) right).getValue();
		else if (right instanceof PyLong)
			return int___long__().__rshift__(right);
		else
			return null;

		if (rightv < 0) {
			throw Py.ValueError("negative shift count");
        }

        if (rightv >= Integer.SIZE) {
            if (getValue() < 0) {
                return Py.newInteger(-1);
            } else {
                return Py.newInteger(0);
            }
        }

		return Py.newInteger(getValue() >> rightv);
	}

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rrshift___doc)
    final PyObject int___rrshift__(PyObject left) {
        int leftv;
        if (left instanceof PyInteger)
        	leftv = ((PyInteger)left).getValue();
        else if(left instanceof PyLong)
        	return left.__rshift__(int___long__());
        else
             return null;

        if(getValue() < 0) {
            throw Py.ValueError("negative shift count");
        }

        if (getValue() >= Integer.SIZE) {
            if (leftv < 0) {
                return Py.newInteger(-1);
            } else {
                return Py.newInteger(0);
            }
        }

        return Py.newInteger(leftv >> getValue());
    }

    public PyObject __and__(PyObject right) {
        return int___and__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___and___doc)
    final PyObject int___and__(PyObject right) {
        int rightv;
        if (right instanceof PyInteger)
             rightv = ((PyInteger) right).getValue();
		else if (right instanceof PyLong)
			return int___long__().__and__(right);
        else
             return null;

        return Py.newInteger(getValue() & rightv);
    }
    
    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rand___doc)
    final PyObject int___rand__(PyObject left){
    	return int___and__(left);
    }

    public PyObject __xor__(PyObject right) {
        return int___xor__(right);
    }
    
    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___xor___doc)
	final PyObject int___xor__(PyObject right) {
		int rightv;
		if (right instanceof PyInteger)
			rightv = ((PyInteger) right).getValue();
		else if (right instanceof PyLong)
			return int___long__().__xor__(right);
		else
			return null;

		return Py.newInteger(getValue() ^ rightv);
    }
    
    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rxor___doc)
	final PyObject int___rxor__(PyObject left){
        int leftv;
        if (left instanceof PyInteger)
        	leftv = ((PyInteger)left).getValue();
        else if(left instanceof PyLong)
        	return left.__rxor__(int___long__());
        else
             return null;

        return Py.newInteger(leftv ^ getValue());
    }

    public PyObject __or__(PyObject right) {
        return int___or__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___or___doc)
	final PyObject int___or__(PyObject right) {
		int rightv;
		if (right instanceof PyInteger)
			rightv = ((PyInteger) right).getValue();
		else if (right instanceof PyLong)
			return int___long__().__or__(right);
		else
			return null;

		return Py.newInteger(getValue() | rightv);
    }
    
    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___ror___doc)
    final PyObject int___ror__(PyObject left){
        return int___or__(left);
    }

    public PyObject __neg__() {
        return int___neg__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___neg___doc)
    final PyObject int___neg__() {
        long x = -getValue();
        return Py.newInteger(x);
    }

    public PyObject __pos__() {
        return int___pos__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___pos___doc)
    final PyObject int___pos__() {
        return int___int__();
    }

    public PyObject __abs__() {
        return int___abs__();
    }
    
    @ExposedMethod(doc = BuiltinDocs.int___abs___doc)
    final PyObject int___abs__() {
        if (getValue() < 0) {
            return int___neg__();
        }
        return int___int__();
    }

    public PyObject __invert__() {
        return int___invert__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___invert___doc)
    final PyObject int___invert__() {
        return Py.newInteger(~getValue());
    }

    public PyObject __int__() {
        return int___int__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___int___doc)
    final PyInteger int___int__() {
        if (getType() == TYPE) {
            return this;
        }
        return Py.newInteger(getValue());
    }

    public PyObject __long__() {
        return int___long__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___long___doc)
    final PyObject int___long__() {
        return new PyLong(getValue());
    }

    public PyFloat __float__() {
        return int___float__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___float___doc)
    final PyFloat int___float__() {
        return new PyFloat((double)getValue());
    }

    public PyComplex __complex__() {
        return new PyComplex(getValue(), 0.);
    }

    public PyString __oct__() {
        return int___oct__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___oct___doc)
    final PyString int___oct__() {
        if (getValue() < 0) {
            return new PyString("-0" + Integer.toString(getValue() * -1, 8));
        }
        else {
            return new PyString("0" + Integer.toString(getValue(), 8));
        }
    }

    public PyString __hex__() {
        return int___hex__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___hex___doc)
    final PyString int___hex__() {
        if (getValue() < 0) {
            return new PyString("-0x" + Integer.toString(getValue() * -1, 16));
        }
        else {
            return new PyString("0x" + Integer.toString(getValue(), 16));
        }
    }
    
    @ExposedMethod(doc = BuiltinDocs.int___getnewargs___doc)
    final PyTuple int___getnewargs__() {
        return new PyTuple(new PyObject[]{new PyInteger(this.getValue())});
    }

    public PyTuple __getnewargs__() {
        return int___getnewargs__();
    }

    @Override
    public PyObject __index__() {
        return int___index__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___index___doc)
    final PyObject int___index__() {
        return this;
    }

    @Override
    public boolean isNumberType() {
        return true;
    }

    @Override
    public boolean isIndex() {
        return true;
    }

    @Override
    public int asIndex(PyObject err) {
        return getValue();
    }

    public boolean isMappingType() { return false; }
    public boolean isSequenceType() { return false; }

    public long asLong(int index) {
        return getValue();
    }

    public int asInt(int index) {
        return getValue();
    }

    @Override
    public int asInt() {
        return getValue();
    }

    @Override
    public long asLong() {
        return getValue();
    }
}
