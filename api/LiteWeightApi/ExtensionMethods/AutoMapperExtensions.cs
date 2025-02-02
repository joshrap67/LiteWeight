using System.Linq.Expressions;
using AutoMapper;

namespace LiteWeightAPI.ExtensionMethods;

public static class AutoMapperExtensions
{
	public static IMappingExpression<TSource, TDestination> Ignore<TSource, TDestination>(
		this IMappingExpression<TSource, TDestination> mapping, Expression<Func<TDestination, object>> propertySelector)
	{
		return mapping.ForMember(propertySelector, opt => opt.Ignore());
	}
}